package services.server;

import DTO.Connection;
import models.File;
import models.User;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import services.server.admin.UserService;
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
            System.out.println("Client handler connected: " + clientSocket);
            System.out.println("Server thread number " + clientNumber + " Started");

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
                case "GET_ALL_USER" -> {
                    List<User> response = getUserList();
                    sendResponse(response);
                }
                case "GET_USER_BY_ID" -> {
                }
                case "GET_ALL_ITEM_PRIVATE" -> {
                    String ownerId = (String) receiveRequest();
                    String searchText = (String) receiveRequest();
                    List<File> response = getPrivateItemList(Integer.parseInt(ownerId), searchText);
                    sendResponse(response);
                }
                case "GET_ALL_ITEM_OSHARE" -> {
                    String ownerId = (String) receiveRequest();
                    String searchText = (String) receiveRequest();
                    List<File> response = getOtherShareItemList(Integer.parseInt(ownerId), searchText);
                    sendResponse(response);
                }
                case "GET_ALL_ITEM_SHARED" -> {
                    String ownerId = (String) receiveRequest();
                    String searchText = (String) receiveRequest();
                    List<File> response = getSharedItemList(Integer.parseInt(ownerId), searchText);
                    sendResponse(response);
                }
                case "GET_ALL_ITEM" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());
                    String searchText = (String) receiveRequest();
                    List<File> response = getItemList(userId, folderId, searchText);
                    sendResponse(response);
                }
                case "GET_ALL_DELETED_ITEM" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    String searchText = (String) receiveRequest();
                    List<File> response = getDeletedItemList(userId, searchText);
                    sendResponse(response);
                }
                case "CREATE_FOLDER" -> {
                    String folderName = (String) receiveRequest();
                    int ownerId = Integer.parseInt((String) receiveRequest());
                    int currentFolderId = Integer.parseInt((String) receiveRequest());
                    boolean response = new FolderService().createFolder(folderName, ownerId, currentFolderId);
                    sendResponse(response);
                }
                case "UPLOAD_FILE" -> {
                    String type = (String) receiveRequest();
                    if(type.equals("file")){
                        String fileName = (String) receiveRequest();
                        int ownerId = Integer.parseInt((String) receiveRequest());
                        int currentFolderId = Integer.parseInt((String) receiveRequest());
                        int fileSize = Integer.parseInt((String) receiveRequest());
                        boolean response = uploadFile(fileName, ownerId, currentFolderId, fileSize);
                        sendResponse(response);
                    } else {
                        System.out.println("Unknown request: " + type);
                    }
                }
                case "UPLOAD_FOLDER" -> {
                    String type_request = (String) receiveRequest();
                    if(type_request.equals("folder")){
                        String folderName = (String) receiveRequest();
                        int ownerId = Integer.parseInt((String) receiveRequest());
                        int currentFolderId = Integer.parseInt((String) receiveRequest());

                        PermissionService permissionService = new PermissionService();
                        int permission_type = permissionService.getPermission(PermissionService.FOLDER_TYPE, currentFolderId);
                        boolean response = uploadFolder(folderName, ownerId, currentFolderId, permission_type);
                        sendResponse(response);
                    } else {
                        System.out.println("Unknown request: " + type_request);
                    }
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
                case "SYNCHRONIZE_FOLDER" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());

                    services.server.user.UserService userService = new services.server.user.UserService();
                    String userPath = userService.getUserPath(userId);

                    services.server.user.FolderService folderService = new services.server.user.FolderService();
                    String path = folderService.getPath(folderId);
                    sendResponse(userPath + java.io.File.separator + path);

                    boolean response = syncFolder(userId, folderId, folderService.getFolderPath(folderId));

                    sendResponse(response);
                }
                case "SYNCHRONIZE_FILE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int fileId = Integer.parseInt((String) receiveRequest());

                    services.server.user.UserService userService = new services.server.user.UserService();
                    String userPath = userService.getUserPath(userId);

                    services.server.user.FileService fileService = new services.server.user.FileService();
                    String path = fileService.getPath(fileId);
                    sendResponse(userPath + java.io.File.separator + path);

                    String filePath = fileService.getFilePath(fileId);
                    int size = fileService.getSize(fileId);
                    sendResponse(String.valueOf(size));

                    syncFile(filePath, size);
                    sendResponse(true);
                }
                case "OPEN_FOLDER" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());

                    String userPath = new services.server.user.UserService().getUserPath(userId);
                    String folderPath = new services.server.user.FolderService().getFolderPath(folderId);
                    sendResponse(userPath + java.io.File.separator + folderPath);

                    boolean response = syncFolder(userId, folderId, new services.server.user.FolderService().getFolderPath(folderId));

                    sendResponse(response);
                }
                case "OPEN_FILE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int fileId = Integer.parseInt((String) receiveRequest());

                    String userPath = new services.server.user.UserService().getUserPath(userId);
                    String filePath = new services.server.user.FileService().getPath(fileId);
                    sendResponse(userPath + java.io.File.separator + filePath);

                    int size = new services.server.user.FileService().getSize(fileId);
                    sendResponse(String.valueOf(size));

                    syncFile(new FileService().getFilePath(fileId), size);

                    boolean response = new RecentFileService().addRecentFile(userId, fileId);
                    sendResponse(response);
                }
                case "SEARCH_UNSHARED_USER" -> {
                    int itemTypeId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    String searchText = (String) receiveRequest();
                    PermissionService permissionService = new PermissionService();
                    List<User> response = permissionService.searchUnsharedUser(itemTypeId, itemId, searchText);
                    sendResponse(response);
                }
                case "SHARE" -> {
                    int itemTypeId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    int permissionType = Integer.parseInt((String) receiveRequest());
                    int sharedBy = Integer.parseInt((String) receiveRequest());
                    int userListSize = Integer.parseInt((String) receiveRequest());
                    ArrayList<Integer> userList = new ArrayList<>();
                    for(int i = 0; i < userListSize; i++){
                        userList.add(Integer.parseInt((String) receiveRequest()));
                    }
                    PermissionService permissionService = new PermissionService();
                    boolean response = permissionService.share(itemTypeId, itemId, permissionType, sharedBy, userList);
                    sendResponse(response);
                }
                case "GET_SHARED_USER" -> {
                    int itemTypeId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    PermissionService permissionService = new PermissionService();
                    List<User> response = permissionService.getSharedUser(itemTypeId, itemId);
                    sendResponse(response);
                }
                case "CHECK_PERMISSION" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int typeId = Integer.parseInt((String) receiveRequest());
                    int id = Integer.parseInt((String) receiveRequest());
                    PermissionService permissionService = new PermissionService();
                    int response = permissionService.checkPermission(userId, typeId, id);
                    sendResponse(response);
                }
                case "GET_PERMISSION" -> {
                    int itemTypeId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    PermissionService permissionService = new PermissionService();
                    int response = permissionService.getPermission(itemTypeId, itemId);
                    System.out.println("Server get permission: " + response);
                    sendResponse(response);
                }
                case "GET_OWNER_ID" -> {
                    int itemTypeId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    int response = new PermissionService().getOwnerId(itemTypeId, itemId);
                    System.out.println("Owner id: " + response);
                    sendResponse(response);
                }
                case "UPDATE_PERMISSION" -> {
                    int itemTypeId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    int finalPermissionId = Integer.parseInt((String) receiveRequest());
                    PermissionService permissionService = new PermissionService();
                    boolean response = permissionService.updatePermission(itemTypeId, itemId, finalPermissionId);
                    sendResponse(response);
                }
                case "DELETE" -> {
                    int itemTypeId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    int userId = Integer.parseInt((String) receiveRequest());
                    boolean response = false;
                    if(itemTypeId == 1){
                        FolderService folderService = new FolderService();
                        response = folderService.deleteFolder(itemId, userId);
                    } else {
                        FileService fileService = new FileService();
                        response = fileService.deleteFile(itemId, userId);
                    }
                    sendResponse(response);
                }
                case "DELETE_PERMANENTLY" -> {
                    int itemTypeId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean response = false;
                    if(itemTypeId == 1){
                        FolderService folderService = new FolderService();
                        response = folderService.deleteFolderPermanently(itemId);
                    } else {
                        FileService fileService = new FileService();
                        response = fileService.deleteFilePermanently(itemId);
                    }
                    sendResponse(response);
                }
                case "RESTORE" -> {
                    int itemTypeId = Integer.parseInt((String) receiveRequest());
                    int itemId = Integer.parseInt((String) receiveRequest());
                    boolean response = false;
                    if(itemTypeId == 1){
                        FolderService folderService = new FolderService();
                        response = folderService.restoreFolder(itemId);
                    } else {
                        FileService fileService = new FileService();
                        response = fileService.restoreFile(itemId);
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

    private List<File> getDeletedItemList(int userId, String searchText) {
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

            syncFile(fileService.getFilePath(fileId), size);
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
            folderService.deleteFolderIfExist(folderPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendZipFolder(String zipFilePath, int size) {
//        byte[] buffer = new byte[1024];
//
//        try(FileInputStream fileInputStream = new FileInputStream(zipFilePath)) {
//            OutputStream fileOutputStream = clientSocket.getOutputStream();
//            int bytesRead;
//            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1) {
//                fileOutputStream.write(buffer, 0, bytesRead);
//                size -= bytesRead;
//            }
//            fileOutputStream.flush();
//            System.out.println("File sent: " + zipFilePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        syncFile(zipFilePath, size);
    }

    private List<User> getUserList() {
        UserService userService = new UserService();
        return userService.getAllUser();
    }

    private List<File> getItemList(int userId, int folderId){
        return getItemList(userId, folderId, "");
    }
    private List<File> getItemList(int userId, int folderId, String searchText) {
        ItemService itemService = new ItemService();
        return itemService.getAllItem(userId, folderId, searchText);
    }
    
    private boolean uploadFile(String fileName, int ownerId, int folderId, int size){
        try{
            int indexOfDot = fileName.indexOf(".");
            String nameOfFile = fileName.substring(0, indexOfDot);
            String typeOfFile = fileName.substring(indexOfDot + 1);

            TypeService typeService = new TypeService();
            int fileTypeId = typeService.getTypeId(typeOfFile);

            FileService fileService = new FileService();
            String rs = fileService.uploadFile(nameOfFile, fileTypeId, folderId, ownerId, size);
            boolean response = false;
            if(!rs.equals("")){
                receiveFile(rs, size);
                System.out.println("Thêm file " + fileName + " thành công");
                response = true;
//                Thread receiveFileThread = new Thread(() -> {
//                    receiveFile(rs, size);
//                });
//                receiveFileThread.start();
            } else {
                System.out.println("Thêm file " + fileName + " thất bại");
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
        List<File> fileList = getItemList(userId, folderId);
        try {
            if(fileList != null){
                for (File file : fileList) {
                    if (file.getTypeId() == 1) {
                        String folderName = file.getName();
                        sendResponse("folder");
                        sendResponse(folderName);
                        syncFolder(userId, file.getId(), folderPath + java.io.File.separator + folderName);
                    } else {
                        String fileName = file.getName() + "." + file.getTypesByTypeId().getName();
                        String filePath = folderPath + java.io.File.separator + fileName;
                        int size = file.getSize() == null ? 0 : file.getSize();
                        sendResponse("file");
                        sendResponse(fileName);
                        sendResponse(String.valueOf(size));
                        syncFile(filePath, size);
                    }
                }
            }
            sendResponse("END_FOLDER");
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

        List<File> fileList = getItemList(userId, folderId);
        try{
            if(fileList != null){
                for (File file : fileList) {
                    if (file.getTypeId() == 1) {
                        createCopyFolderTemp(userId, file.getId(), folderPath);
                    } else {
                        String fileName = file.getName() + "." + file.getTypesByTypeId().getName();
                        String newFilePath = folderPath + java.io.File.separator + fileName;
                        FileService fileService = new FileService();
                        String realFilePath = fileService.getFilePath(file.getId());
                        createCopyFileTemp(realFilePath, newFilePath);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean uploadFolder(String folderName, int ownerId, int parentId, int permission_type) throws IOException, ClassNotFoundException {
        FolderService folderService = new FolderService();
        int rs = folderService.uploadFolder(folderName, ownerId, parentId, permission_type);
        sendResponse(String.valueOf(rs));

        boolean response = true;
        boolean check = true;

        if(rs != -1){
            System.out.println("Tạo folder "+ folderName + " thành công");
        } else {
            System.out.println("Tạo folder "+ folderName + " thất bại");
            return false;
        }

        // receive file and folder
        String child_type = (String) receiveRequest();
        while(!child_type.equals("END_FOLDER")){
            if(child_type.equals("folder")){
                String folderNameOfChild = (String) receiveRequest();
                int ownerIdOfChild = Integer.parseInt((String) receiveRequest());
                int parentIdOfChild = Integer.parseInt((String) receiveRequest());
                response = uploadFolder(folderNameOfChild, ownerIdOfChild, parentIdOfChild, permission_type);
                if(!response) check = false;
            } else if(child_type.equals("file")){
                String fileName = (String) receiveRequest();
                int ownerIdOfFile = Integer.parseInt((String) receiveRequest());
                int parentIdOfFile = Integer.parseInt((String) receiveRequest());
                int sizeOfFile = Integer.parseInt((String) receiveRequest());

                response = uploadFile(fileName, ownerIdOfFile, parentIdOfFile, sizeOfFile);
                if(!response) check = false;
            }
            child_type = (String) receiveRequest();
        }

        if(check){
            System.out.println("Upload folder "+ folderName + " thành công");
        } else {
            System.out.println("Upload folder "+ folderName + " thất bại");
        }
        return check;
    }
    
    private User getUserById(int id) {
        UserService userService = new UserService();
        System.out.println("Get user by id");
        return userService.getUserById(id);
    }
    private List<File> getPrivateItemList(int ownerId, String searchText) {
        ItemService itemService = new ItemService();
        System.out.println("Get all private item");
        return itemService.getAllItemPrivateOwnerId(ownerId, searchText);
    }
    private List<File> getOtherShareItemList(int ownerId, String searchText) {
        ItemService itemService = new ItemService();
        System.out.println("Get all other share item");
        return itemService.getAllOtherShareItem(ownerId, searchText);
    }
    private List<File> getSharedItemList(int ownerId, String searchText) {
        ItemService itemService = new ItemService();
        System.out.println("Get all shared item");
        return itemService.getAllSharedItem(ownerId, searchText);
    }

    public void sendResponse(Object response) {
        try{
            out.writeObject(response);
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
