package services.server;

import DTO.*;
import applications.ServerApp;
import enums.PermissionType;
import enums.TypeEnum;
import enums.UploadStatus;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import services.server.auth.LoginService;
import services.server.user.*;
import utils.ZipFolder;

import static applications.ServerApp.connections;

public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private InetAddress clientAddress;

    public ClientHandler(Socket clientSocket, int clientNumber) {
        try{
            this.clientSocket = clientSocket;
            clientAddress = clientSocket.getInetAddress();

            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            addConnection("CONNECTED");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Object obj = receiveRequest();

            String request = "";
            if (obj instanceof String) {
                request = (String) obj;
                System.out.println("Client request: " + request);
            } else {
                System.out.println("Unknown request 1: " + obj);
            }
            addConnection(request);

            switch (request) {
                case "LOGIN" -> {
                    String username = (String) receiveRequest();
                    String password = (String) receiveRequest();
                    UserSession response = new LoginService().validate(username, password);
                    sendResponse(response);
                }
                case "GET_USER_ACCOUNT_INFO" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    UserAccountDTO response = new AccountService().getUserAccountInfo(userId);
                    sendResponse(response);
                }
                case "UPDATE_PASSWORD" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    String oldPassword = (String) receiveRequest();
                    String newPassword = (String) receiveRequest();
                    boolean response = new AccountService().updatePassword(userId, oldPassword, newPassword);
                    sendResponse(response);
                }
                case "GET_ALL_USER" -> {
                    List<UserDTO> response = getUserList();
                    sendResponse(response);
                }
                case "GET_ALL_ITEM_PRIVATE" -> {
                    String ownerId = (String) receiveRequest();
                    String searchText = (String) receiveRequest();
                    List<ItemDTO> response = getPrivateItemList(Integer.parseInt(ownerId), searchText);
                    sendResponse(response);
                }
                case "GET_ALL_ITEM_OSHARE" -> {
                    String ownerId = (String) receiveRequest();
                    String searchText = (String) receiveRequest();
                    List<ItemDTO> response = getOtherShareItemList(Integer.parseInt(ownerId), searchText);
                    sendResponse(response);
                }
                case "GET_ALL_ITEM_SHARED" -> {
                    String ownerId = (String) receiveRequest();
                    String searchText = (String) receiveRequest();
                    List<ItemDTO> response = getSharedItemList(Integer.parseInt(ownerId), searchText);
                    sendResponse(response);
                }
                case "GET_ALL_ITEM" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());
                    String searchText = (String) receiveRequest();
                    List<ItemDTO> response = getItemList(userId, folderId, searchText);
                    sendResponse(response);
                }
                case "GET_ALL_RECENT_OPENED_ITEM" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    String searchText = (String) receiveRequest();
                    List<RecentFileDTO> response = new RecentFileService().getAllRecentOpenedItem(userId, searchText);
                    sendResponse(response);
                }
                case "GET_ALL_DELETED_ITEM" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    String searchText = (String) receiveRequest();
                    List<ItemDeletedDTO> response = getDeletedItemList(userId, searchText);
                    sendResponse(response);
                }
                case "CREATE_FOLDER" -> {
                    String folderName = (String) receiveRequest();
                    int ownerId = Integer.parseInt((String) receiveRequest());
                    int currentFolderId = Integer.parseInt((String) receiveRequest());
                    int response = new FolderService().createFolder(folderName, ownerId, currentFolderId);
                    sendResponse(response);
                }
    			case "RENAME" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    String newName = (String) receiveRequest();
                    boolean response = false;
                    if(isFolder){
                        boolean isRenameInDB = new FolderService().renameFolder(userId, itemId, newName);
                        if(isRenameInDB){
                            FolderService.renameFolderInPath(itemId, newName);
                            response = true;
                        }
                    } else {
                        boolean isRenameInDB = new FileService().renameFile(userId, itemId, newName);
                        if(isRenameInDB){
                            FileService.renameFileInPath(itemId, newName);
                            response = true;
                        }
                    }
    				sendResponse(response);
    			}
                case "UPLOAD_FILE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    String fileName = (String) receiveRequest();
                    int folderId = Integer.parseInt((String) receiveRequest());
                    int fileSize = Integer.parseInt((String) receiveRequest());
                    int checkBeforeUpload = checkUploadFileStatus(fileName, folderId, false);
                    if(checkBeforeUpload != UploadStatus.SUCCESS.getValue()){
                        sendResponse(checkBeforeUpload);
                        break;
                    }
                    int permission = new PermissionService().checkUserPermission(userId, folderId, true);
                    if (permission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }
                    sendResponse(UploadStatus.PERMISSION_ACCEPTED.getValue());
                    int response = uploadFile(fileName, userId, folderId, fileSize);
                    sendResponse(response);
                }
                case "UPLOAD_FILE_AND_REPLACE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    String fileName = (String) receiveRequest();
                    int folderId = Integer.parseInt((String) receiveRequest());
                    int fileSize = Integer.parseInt((String) receiveRequest());
                    int checkBeforeUpload = checkUploadFileStatus(fileName, folderId, true);
                    if(checkBeforeUpload != UploadStatus.SUCCESS.getValue()){
                        sendResponse(checkBeforeUpload);
                        break;
                    }
                    int permission = new PermissionService().checkUserPermission(userId, folderId, true);
                    if (permission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }
                    sendResponse(UploadStatus.PERMISSION_ACCEPTED.getValue());
                    int response = uploadFile(fileName, userId, folderId, fileSize);
                    sendResponse(response);
                }
                case "UPLOAD_FOLDER" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    String folderName = (String) receiveRequest();
                    int parentId = Integer.parseInt((String) receiveRequest());
                    int checkBeforeUpload = checkUploadFolderStatus(folderName, parentId, false);
                    if(checkBeforeUpload != UploadStatus.SUCCESS.getValue()){
                        sendResponse(checkBeforeUpload);
                        break;
                    }
                    int permission = new PermissionService().checkUserPermission(userId, parentId, true);
                    if (permission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }
                    sendResponse(UploadStatus.PERMISSION_ACCEPTED.getValue());
                    int publicPermission = new PermissionService().getPublicPermission(parentId, true);
                    int response = uploadFolder(folderName, userId, parentId, publicPermission);
                    sendResponse(response);
                }
                case "UPLOAD_FOLDER_AND_REPLACE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    String folderName = (String) receiveRequest();
                    int parentId = Integer.parseInt((String) receiveRequest());
                    int checkBeforeUpload = checkUploadFolderStatus(folderName, parentId, true);
                    if(checkBeforeUpload != UploadStatus.SUCCESS.getValue()){
                        sendResponse(checkBeforeUpload);
                        break;
                    }
                    int permission = new PermissionService().checkUserPermission(userId, parentId, true);
                    if (permission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }
                    sendResponse(UploadStatus.PERMISSION_ACCEPTED.getValue());
                    int publicPermission = new PermissionService().getPublicPermission(parentId, true);
                    int response = uploadFolder(folderName, userId, parentId, publicPermission);
                    sendResponse(response);
                }
                case "DOWNLOAD_FILE" -> {
                    int fileId = Integer.parseInt((String) receiveRequest());

                    boolean response = downloadFile(fileId);

                    sendResponse(response);
                }
                case "DOWNLOAD_FOLDER" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());

                    boolean response =  downloadFolder(userId, folderId);

                    sendResponse(response);
                }
                case "SYNCHRONIZE_FILE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int fileId = Integer.parseInt((String) receiveRequest());

                    String userPath = new UserService().getUserPath(userId);
                    FileService fileService = new FileService();
                    String path = fileService.getPath(fileId);
                    sendResponse(userPath + java.io.File.separator + path);

                    String filePath = FileService.getFilePath(fileId);
                    int size = fileService.getSize(fileId);
                    sendResponse(String.valueOf(size));

                    syncFile(filePath, size);
                    sendResponse(true);
                }
                case "SYNCHRONIZE_FOLDER" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());

                    String userPath = new UserService().getUserPath(userId);

                    String path = FolderService.getPath(folderId);
                    sendResponse(userPath + java.io.File.separator + path);

                    boolean response = syncFolder(userId, folderId, FolderService.getFolderPath(folderId));

                    sendResponse(response);
                }
                case "OPEN_FILE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int fileId = Integer.parseInt((String) receiveRequest());

                    String userPath = new UserService().getUserPath(userId);
                    String filePath = new FileService().getPath(fileId);
                    sendResponse(userPath + java.io.File.separator + filePath);

                    int size = new FileService().getSize(fileId);
                    sendResponse(String.valueOf(size));

                    syncFile(FileService.getFilePath(fileId), size);

                    boolean response = new RecentFileService().addRecentFile(userId, fileId);
                    sendResponse(response);
                }
                case "GET_ALL_ITEM_POPS_UP" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());
                    List<MoveCopyFolderDTO> response = new ItemService().getAllFolderPopups(userId, itemId, isFolder, folderId);
                    sendResponse(response);
                }
                case "MOVE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int targetId = Integer.parseInt((String) receiveRequest());
                    int permission = new PermissionService().checkUserPermission(userId, itemId, isFolder);
                    if (permission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }
                    int targetPermission = new PermissionService().checkUserPermission(userId, targetId, isFolder);
                    if (targetPermission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }
                    if (isFolder) {
                        String folderName = FolderService.getFolderNameById(itemId);
                        if (FolderService.checkFolderExist(folderName, targetId) || FolderService.checkFolderExistInPath(folderName, targetId)) {
                            sendResponse(UploadStatus.EXISTED.getValue());
                            break;
                        }
                    } else {
                        String fileName = new FileService().getFullNameById(itemId);
                        if (FileService.checkFileExist(fileName, targetId) || FileService.checkFileExistInPath(fileName, targetId)) {
                            sendResponse(UploadStatus.EXISTED.getValue());
                            break;
                        }
                    }
                    int response;
                    if(isFolder){
                        String beforePath = FolderService.getFolderPath(itemId);
                        boolean isMoveInDB = new FolderService().moveFolder(itemId, targetId);
                        if(isMoveInDB){
                            FolderService.moveFolderInPath(beforePath, targetId);
                            response = UploadStatus.SUCCESS.getValue();
                        } else {
                            response = UploadStatus.FAILED.getValue();
                        }
                    } else {
                        String beforePath = FileService.getFilePath(itemId);
                        boolean isMoveInDB = new FileService().moveFile(itemId, targetId);
                        if(isMoveInDB){
                            FileService.moveFileInPath(beforePath, targetId);
                            response = UploadStatus.SUCCESS.getValue();
                        } else {
                            response = UploadStatus.FAILED.getValue();
                        }
                    }
                    sendResponse(response);
                }
                case "MOVE_AND_REPLACE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int targetId = Integer.parseInt((String) receiveRequest());
                    int permission = new PermissionService().checkUserPermission(userId, itemId, isFolder);
                    if (permission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }
                    int targetPermission = new PermissionService().checkUserPermission(userId, targetId, isFolder);
                    if (targetPermission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }

                    int response;
                    if(isFolder){
                        String beforePath = FolderService.getFolderPath(itemId);
                        boolean isMoveInDB = new FolderService().moveFolder(itemId, targetId);
                        if(isMoveInDB){
                            FolderService.moveFolderInPath(beforePath, targetId);
                            response = UploadStatus.SUCCESS.getValue();
                        } else {
                            response = UploadStatus.FAILED.getValue();
                        }
                    } else {
                        String beforePath = FileService.getFilePath(itemId);
                        boolean isMoveInDB = new FileService().moveFile(itemId, targetId);
                        if(isMoveInDB){
                            FileService.moveFileInPath(beforePath, targetId);
                            response = UploadStatus.SUCCESS.getValue();
                        } else {
                            response = UploadStatus.FAILED.getValue();
                        }
                    }
                    sendResponse(response);
                }
                case "COPY" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int targetId = Integer.parseInt((String) receiveRequest());

                    int permission = new PermissionService().checkUserPermission(userId, itemId, isFolder);
                    if (permission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }
                    int targetPermission = new PermissionService().checkUserPermission(userId, targetId, isFolder);
                    if (targetPermission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }

                    if (isFolder) {
                        String folderName = FolderService.getFolderNameById(itemId);
                        if (FolderService.checkFolderExist(folderName, targetId) || FolderService.checkFolderExistInPath(folderName, targetId)) {
                            sendResponse(UploadStatus.EXISTED.getValue());
                            break;
                        }
                    } else {
                        String fileName = new FileService().getFullNameById(itemId);
                        if (FileService.checkFileExist(fileName, targetId) || FileService.checkFileExistInPath(fileName, targetId)) {
                            sendResponse(UploadStatus.EXISTED.getValue());
                            break;
                        }
                    }

                    int response;
                    if(isFolder){
                        String beforePath = FolderService.getFolderPath(itemId);
                        boolean isCopyInDB = new FolderService().copyFolder(itemId, targetId);
                        if(isCopyInDB){
                            FolderService.copyFolderInPath(beforePath, targetId);
                            response = UploadStatus.SUCCESS.getValue();
                        } else {
                            response = UploadStatus.FAILED.getValue();
                        }
                    } else {
                        String beforePath = FileService.getFilePath(itemId);
                        boolean isCopyInDB = new FileService().copyFile(itemId, targetId);
                        if(isCopyInDB){
                            FileService.copyFileInPath(beforePath, targetId);
                            response = UploadStatus.SUCCESS.getValue();
                        } else {
                            response = UploadStatus.FAILED.getValue();
                        }
                    }
                    sendResponse(response);
                }
                case "COPY_AND_REPLACE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int targetId = Integer.parseInt((String) receiveRequest());

                    int permission = new PermissionService().checkUserPermission(userId, itemId, isFolder);
                    if (permission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }
                    int targetPermission = new PermissionService().checkUserPermission(userId, targetId, isFolder);
                    if (targetPermission <= PermissionType.READ.getValue()) {
                        sendResponse(UploadStatus.PERMISSION_DENIED.getValue());
                        break;
                    }

                    int response;
                    if(isFolder){
                        String beforePath = FolderService.getFolderPath(itemId);
                        boolean isCopyInDB = new FolderService().copyFolder(itemId, targetId);
                        if(isCopyInDB){
                            FolderService.copyFolderInPath(beforePath, targetId);
                            response = UploadStatus.SUCCESS.getValue();
                        } else {
                            response = UploadStatus.FAILED.getValue();
                        }
                    } else {
                        String beforePath = FileService.getFilePath(itemId);
                        boolean isCopyInDB = new FileService().copyFile(itemId, targetId);
                        if(isCopyInDB){
                            FileService.copyFileInPath(beforePath, targetId);
                            response = UploadStatus.SUCCESS.getValue();
                        } else {
                            response = UploadStatus.FAILED.getValue();
                        }
                    }
                    sendResponse(response);
                }
                case "OPEN_FOLDER" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());

                    String userPath = new UserService().getUserPath(userId);
                    String folderPath = FolderService.getFolderPath(folderId);
                    sendResponse(userPath + java.io.File.separator + folderPath);

                    boolean response = syncFolder(userId, folderId, FolderService.getFolderPath(folderId));
                    sendResponse(response);
                }
                case "GET_SHARED_PERMISSION" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int response = new PermissionService().getSharedPermission(itemId, isFolder);
                    sendResponse(response);
                }
                case "GET_SHARED_USER" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    List<UserToShareDTO> response = new PermissionService().getSharedUser(itemId, isFolder);
                    sendResponse(response);
                }
                case "DELETE_SHARED_USER" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int userId = Integer.parseInt((String) receiveRequest());
                    boolean response = new PermissionService().deleteSharedUser(itemId, isFolder, userId);
                    sendResponse(response);
                }
                case "UPDATE_SHARED_PERMISSION" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int permissionType = Integer.parseInt((String) receiveRequest());
                    int ownerId = Integer.parseInt((String) receiveRequest());
                    PermissionService permissionService = new PermissionService();
                    if(!(permissionType <= PermissionType.WRITE.getValue())){
                        sendResponse(false);
                        break;
                    }
                    int permissionOfUser = permissionService.checkUserPermission(ownerId, itemId, isFolder);
                    if(permissionOfUser != PermissionType.OWNER.getValue()){
                        sendResponse(false);
                        break;
                    }
                    boolean response = permissionService.updateSharedPermission(itemId, isFolder, permissionType);
                    sendResponse(response);
                }
                case "SEARCH_UNSHARED_USER" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    String searchText = (String) receiveRequest();
                    PermissionService permissionService = new PermissionService();
                    List<UserToShareDTO> response = permissionService.searchUnsharedUser(itemId, isFolder, searchText);
                    sendResponse(response);
                }
                case "SHARE" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int permissionType = Integer.parseInt((String) receiveRequest());
                    int sharedBy = Integer.parseInt((String) receiveRequest());
                    int userListSize = Integer.parseInt((String) receiveRequest());
                    ArrayList<Integer> userList = new ArrayList<>();
                    for(int i = 0; i < userListSize; i++){
                        userList.add(Integer.parseInt((String) receiveRequest()));
                    }
                    if(!(permissionType == PermissionType.READ.getValue() || permissionType == PermissionType.WRITE.getValue())){
                        sendResponse(false);
                        break;
                    }
                    PermissionService permissionService = new PermissionService();
                    int sharedPermission = permissionService.checkSharedPermission(itemId, isFolder);
                    if(!(permissionType == sharedPermission || sharedPermission == -1)){
                        sendResponse(false);
                        break;
                    }
                    boolean response = permissionService.share(itemId, isFolder, permissionType, sharedBy, userList);
                    sendResponse(response);
                }
                case "CHECK_USER_PERMISSION" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    PermissionService permissionService = new PermissionService();
                    int response = permissionService.checkUserPermission(userId, itemId, isFolder);
                    sendResponse(response);
                }
                case "GET_PUBLIC_PERMISSION" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    PermissionService permissionService = new PermissionService();
                    int response = permissionService.getPublicPermission(itemId, isFolder);
                    sendResponse(response);
                }
                case "GET_OWNER_ID" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int response = new PermissionService().getOwnerId(itemId, isFolder);
                    sendResponse(response);
                }
                case "UPDATE_PUBLIC_PERMISSION" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int finalPermissionId = Integer.parseInt((String) receiveRequest());
                    boolean response = new PermissionService().updatePublicPermission(itemId, isFolder, finalPermissionId);
                    sendResponse(response);
                }
                case "DELETE" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int userId = Integer.parseInt((String) receiveRequest());
                    int permission = new PermissionService().checkUserPermission(userId, itemId, isFolder);
                    if (permission < PermissionType.WRITE.getValue()) {
                        sendResponse(false);
                        break;
                    }
                    boolean response = false;
                    if(isFolder){
                        boolean isDeletedInDB = new FolderService().deleteFolder(itemId, userId);
                        if(isDeletedInDB){
                            FolderService.moveToTrash(itemId);
                            response = true;
                        }
                    } else {
                        boolean isDeletedInDB = new FileService().deleteFile(itemId, userId);
                        if(isDeletedInDB){
                            FileService.moveToTrash(itemId);
                            response = true;
                        }
                    }
                    sendResponse(response);
                }
                case "DELETE_PERMANENTLY" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    boolean response = false;
                    if(isFolder){
                        String pathInTrash = FolderService.getFolderPath(itemId);
                        boolean isDeletedInDB =  new FolderService().deleteFolderPermanently(itemId);
                        if(isDeletedInDB){
                            FolderService.deleteFolderIfExist(pathInTrash);
                            response = true;
                        }
                    } else {
                        String pathInTrash = FileService.getFilePath(itemId);
                        boolean isDeletedInDB = FileService.deleteFilePermanently(itemId);
                        if(isDeletedInDB){
                            FileService.deleteFileIfExist(pathInTrash);
                            response = true;
                        }
                    }
                    sendResponse(response);
                }
                case "RESTORE" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int response = UploadStatus.FAILED.getValue();
                    if(isFolder){
                        FolderService folderService = new FolderService();
                        String finalPath = folderService.getFinalPath(itemId);
                        if(new java.io.File(ServerApp.SERVER_PATH + File.separator + finalPath + File.separator + FolderService.getFolderNameById(itemId)).exists()){
                            sendResponse(UploadStatus.EXISTED.getValue());
                            break;
                        }
                        String folderPath = FolderService.getFolderPath(itemId);
                        if(finalPath == null || finalPath.isEmpty() || !new java.io.File(folderPath).exists()){
                            sendResponse(false);
                            break;
                        }
                        boolean isRestoredInDB = folderService.restoreFolder(itemId);
                        if(isRestoredInDB){
                            FolderService.restoreFolderInPath(itemId, finalPath);
                            response = UploadStatus.SUCCESS.getValue();
                        }
                    } else {
                        FileService fileService = new FileService();
                        String finalPath = fileService.getFinalPath(itemId);
                        if(new java.io.File(ServerApp.SERVER_PATH + File.separator + finalPath + File.separator + fileService.getFullNameById(itemId)).exists()){
                            sendResponse(UploadStatus.EXISTED.getValue());
                            break;
                        }
                        if(finalPath == null || finalPath.isEmpty() || !new java.io.File(finalPath).exists()){
                            sendResponse(false);
                            break;
                        }
                        boolean isRestoredInDB = fileService.restoreFile(itemId);
                        if(isRestoredInDB){
                            FileService.restoreFileInPath(itemId, finalPath);
                            response = UploadStatus.SUCCESS.getValue();
                        }
                    }
                    sendResponse(response);
                }
                case "RESTORE_AND_REPLACE" -> {
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean isFolder = Boolean.parseBoolean((String) receiveRequest());
                    int response = UploadStatus.FAILED.getValue();
                    if(isFolder){
                        FolderService folderService = new FolderService();
                        String finalPath = folderService.getFinalPath(itemId);
                        String folderPath = FolderService.getFolderPath(itemId);
                        if(finalPath == null || finalPath.isEmpty() || !new java.io.File(folderPath).exists()){
                            sendResponse(UploadStatus.FAILED.getValue());
                            break;
                        }
                        boolean isRestoredInDB = folderService.restoreFolder(itemId);
                        if(isRestoredInDB){
                            FolderService.restoreFolderInPath(itemId, finalPath);
                            response = UploadStatus.SUCCESS.getValue();
                        }
                    } else {
                        FileService fileService = new FileService();
                        String finalPath = fileService.getFinalPath(itemId);
                        if(finalPath == null || finalPath.isEmpty() || !new java.io.File(finalPath).exists()){
                            sendResponse(UploadStatus.FAILED.getValue());
                            break;
                        }
                        boolean isRestoredInDB = fileService.restoreFile(itemId);
                        if(isRestoredInDB){
                            FileService.restoreFileInPath(itemId, finalPath);
                            response = UploadStatus.SUCCESS.getValue();
                        }
                    }
                    sendResponse(response);
                }
                default -> {
                    System.out.println("Unknown request: " + request);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null) out.close();
                if(in != null) in.close();
                if(clientSocket != null) clientSocket.close();
                addConnection("DISCONNECTED");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<ItemDeletedDTO> getDeletedItemList(int userId, String searchText) {
        ItemService itemService = new ItemService();
        return itemService.getAllDeletedItem(userId, searchText);
    }


    private boolean downloadFile(int fileId){
        try {
            FileService fileService = new FileService();
            String fileName = fileService.getFullFileName(fileId);
            sendResponse(fileName);

            int size = fileService.getSize(fileId);
            sendResponse(String.valueOf(size));

            syncFile(FileService.getFilePath(fileId), size);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean downloadFolder(int userId, int folderId) {
        try {
            FolderService folderService = new FolderService();
            String folderName = folderService.getFolderName(folderId);
            sendResponse(folderName);

            String downloadFolder = System.getProperty("user.home") + java.io.File.separator + "Downloads";
            createCopyFolderTemp(userId, folderId, downloadFolder);

            String folderPath = downloadFolder + java.io.File.separator + folderName;
            ZipFolder zipFolder = new ZipFolder(folderName, folderPath);
            String zipFilePath = zipFolder.zip();

            int size = (int) zipFolder.size();
            sendResponse(String.valueOf(size));

            sendZipFolder(zipFilePath, size);
            zipFolder.deleteOutputZipFile();
            FolderService.deleteFolderIfExist(folderPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendZipFolder(String zipFilePath, int size) {
        syncFile(zipFilePath, size);
    }

    private List<UserDTO> getUserList() {
        return new UserService().getAllUser();
    }


    private List<ItemDTO> getItemList(int userId, int folderId){
        return getItemList(userId, folderId, "");
    }
    private List<ItemDTO> getItemList(int userId, int folderId, String searchText) {
        ItemService itemService = new ItemService();
        return itemService.getAllItem(userId, folderId, searchText);
    }
    
    private int uploadFile(String fileName, int userId, int folderId, int size){
        try{
            int indexOfDot = fileName.indexOf(".");
            String nameOfFile = fileName.substring(0, indexOfDot);
            String typeOfFile = fileName.substring(indexOfDot + 1);

            int fileTypeId = new TypeService().getTypeId(typeOfFile);

            boolean isUploaded = new FileService().uploadFile(nameOfFile, fileTypeId, folderId, userId, size);
            if(isUploaded){
                String filePath = FolderService.getFolderPath(folderId) + java.io.File.separator + fileName;
                receiveFile(filePath, size);
                System.out.println("Thêm file " + fileName + " thành công");
                return UploadStatus.SUCCESS.getValue();
            } else {
                return UploadStatus.FAILED.getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    private void receiveFile(String filePath, int size){
        byte[] buffer = new byte[1024];

        try(FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            InputStream fileInputStream = clientSocket.getInputStream();
            int bytesRead;
            //while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void syncFile(String filePath, int size){
        byte[] buffer = new byte[1024];

        try(FileInputStream fileInputStream = new FileInputStream(filePath)) {
            OutputStream fileOutputStream = clientSocket.getOutputStream();
            int bytesRead;
            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean syncFolder(int userId, int folderId, String folderPath){
        List<ItemDTO> fileList = getItemList(userId, folderId);
        try {
            if(fileList != null){
                for (ItemDTO file : fileList) {
                    if (file.getTypeId() == 1) {
                        String folderName = file.getName();
                        sendResponse(String.valueOf(TypeEnum.FOLDER.getValue()));
                        sendResponse(folderName);
                        syncFolder(userId, file.getId(), folderPath + java.io.File.separator + folderName);
                    } else {
                        String fileName = file.getName() + "." + file.getTypeName();
                        String filePath = folderPath + java.io.File.separator + fileName;
                        int size = file.getSize();
                        sendResponse(String.valueOf(TypeEnum.FILE.getValue()));
                        sendResponse(fileName);
                        sendResponse(String.valueOf(size));
                        syncFile(filePath, size);
                    }
                }
            }
            sendResponse(String.valueOf(TypeEnum.EOF.getValue()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createCopyFileTemp(String realFilePath, String newFilePath){
        try {
            Files.copy(new java.io.File(realFilePath).toPath(), new java.io.File(newFilePath).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCopyFolderTemp(int userId, int folderId, String newFolderPath){
        FolderService folderService = new FolderService();
        String folderName = folderService.getFolderName(folderId);
        String folderPath = newFolderPath + java.io.File.separator + folderName;
        folderService.createFolderIfNotExist(folderPath);

        List<ItemDTO> fileList = getItemList(userId, folderId);
        try{
            if(fileList != null){
                for (ItemDTO file : fileList) {
                    if (file.getTypeId() == TypeEnum.FOLDER.getValue()) {
                        createCopyFolderTemp(userId, file.getId(), folderPath);
                    } else {
                        String fileName = file.getName() + "." + file.getTypeName();
                        String newFilePath = folderPath + java.io.File.separator + fileName;
                        String realFilePath = FileService.getFilePath(file.getId());
                        createCopyFileTemp(realFilePath, newFilePath);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int checkUploadFileStatus(String fileName, int folderId, boolean isReplace) {
        boolean isExist = FileService.checkFileExist(fileName, folderId);
        if(isExist){
            if(isReplace){
                boolean isDeletedInDB = FileService.deleteFilePermanently(new FileService().getFileIdByFileNameAndFolderId(fileName, folderId));
                if(!isDeletedInDB){
                    return UploadStatus.FAILED.getValue();
                } else {
                    if(FileService.checkFileExistInPath(fileName, folderId)){
                        FileService.deleteFileInPath(fileName, folderId);
                    }
                }
            } else {
                return UploadStatus.EXISTED.getValue();
            }
        } else {
            if(FileService.checkFileExistInPath(fileName, folderId)){
                FileService.deleteFileInPath(fileName, folderId);
            }
        }
        return UploadStatus.SUCCESS.getValue();
    }

    int checkUploadFolderStatus(String folderName, int parentId, boolean isReplace) {
        boolean isExist = FolderService.checkFolderExist(folderName, parentId);
        if(isExist){
            if(isReplace){
                FolderService folderService = new FolderService();
                boolean isDeletedInDB = folderService.deleteFolderPermanently(folderService.getFolderIdByFolderNameAndParentId(folderName, parentId));
                if(!isDeletedInDB){
                    return UploadStatus.FAILED.getValue();
                } else {
                    if(FolderService.checkFolderExistInPath(folderName, parentId)){
                        FolderService.deleteFolderInPath(folderName, parentId);
                    }
                }
            } else {
                return UploadStatus.EXISTED.getValue();
            }
        } else {
            if(FolderService.checkFolderExistInPath(folderName, parentId)){
                FolderService.deleteFolderInPath(folderName, parentId);
            }
        }
        return UploadStatus.SUCCESS.getValue();
    }

    private int uploadFolder(String folderName, int userId, int parentId, int publicPermission) throws IOException, ClassNotFoundException {
        FolderService folderService = new FolderService();
        int folderId = folderService.uploadFolder(userId, folderName, userId, parentId, publicPermission);
        sendResponse(String.valueOf(folderId));

        if(folderId == -1){
            return UploadStatus.FAILED.getValue();
        }

        int isSuccess = UploadStatus.SUCCESS.getValue();

        // receive file and folder
        int itemType = Integer.parseInt((String) receiveRequest());
        while(itemType != TypeEnum.EOF.getValue()){
            if(itemType == TypeEnum.FOLDER.getValue()){
                String folderNameOfChild = (String) receiveRequest();
                int parentIdOfChild = Integer.parseInt((String) receiveRequest());
                if(uploadFolder(folderNameOfChild, userId, parentIdOfChild, publicPermission) != UploadStatus.SUCCESS.getValue()){
                    isSuccess = UploadStatus.FAILED.getValue();
                }
            } else if(itemType == TypeEnum.FILE.getValue()){
                String fileName = (String) receiveRequest();
                int parentIdOfFile = Integer.parseInt((String) receiveRequest());
                int sizeOfFile = Integer.parseInt((String) receiveRequest());

                if(uploadFile(fileName, userId, parentIdOfFile, sizeOfFile) != UploadStatus.SUCCESS.getValue()){
                    isSuccess = UploadStatus.FAILED.getValue();
                }
            }
            itemType = Integer.parseInt((String) receiveRequest());
        }

        return isSuccess;
    }

    private List<ItemDTO> getPrivateItemList(int ownerId, String searchText) {
        ItemService itemService = new ItemService();
        System.out.println("Get all private item");
        return itemService.getAllItemPrivateOwnerId(ownerId, searchText);
    }
    private List<ItemDTO> getOtherShareItemList(int ownerId, String searchText) {
        ItemService itemService = new ItemService();
        System.out.println("Get all other share item");
        return itemService.getAllOtherShareItem(ownerId, searchText);
    }
    private List<ItemDTO> getSharedItemList(int ownerId, String searchText) {
        ItemService itemService = new ItemService();
        System.out.println("Get all shared item");
        return itemService.getAllSharedItem(ownerId, searchText);
    }

    public void sendResponse(Object response) {
        try{
            out.writeObject(response);
            System.out.println("Server response: " + response);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object receiveRequest() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    public void addConnection(String request) {
        Connection connection = new Connection(clientAddress.getHostAddress(), request);
        connections.add(connection);
//        System.out.println("Connection added: " + connection);
//        System.out.println("Connection list: " + connections);
    }
}
