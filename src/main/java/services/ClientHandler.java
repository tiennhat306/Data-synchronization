package services;

import DTO.*;
import applications.ServerApp;
import enums.PermissionType;
import enums.TypeEnum;
import enums.UploadStatus;
import models.User;

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

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private InetAddress clientAddress;



    public ClientHandler(Socket clientSocket, int clientNumber) {
        try {
            this.clientSocket = clientSocket;
            clientAddress = clientSocket.getInetAddress();
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Tạo luồng ra trên socket cho việc gửi dữ liệu
        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        // Phương thức gửi đối tượng Object được chuyển đổi từ các kiểu
        // cấu trúc dữ liệu khác (dataObject)
        outputStream.WriteObject(dataObject);
        // Phương thức đẩy dữ liệu lên luồng
        outputStream.flush();

    }

    @Override
    public void run() {
        try {
            Object obj = receiveRequest();
            String request = "";
            if (obj instanceof String) {
                request = (String) obj;
            } else {
                System.err.println("Unknown request: " + obj);
                return;
            }
            addConnection(request);

            switch (request) {
                // Các trường hợp xử lý khác …
                case "GET_ALL_ITEM" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());
                    String searchText = (String) receiveRequest();
                    List<ItemDTO> response = getItemList(userId, folderId, searchText);
                    sendResponse(response);
                }
                case "UPLOAD_FILE" -> {
                    // Nhận các tham số sau đó kiểm tra quyền hạn, và sự tồn tại của tập tin
                    // Báo lỗi cho Client nếu có
                    // Nếu mọi điều kiện được đáp ứng thì tiến hành nhận file và lưu vào hệ thống
                }
                case "UPLOAD_FOLDER" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    String folderName = (String) receiveRequest();
                    int parentId = Integer.parseInt((String) receiveRequest());
                    int checkBeforeUpload = checkUploadFolderStatus(folderName, parentId, false);
                    if (checkBeforeUpload != UploadStatus.SUCCESS.getValue()) {
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
                case "SYNCHRONIZE_FILE" -> {
                    // Đồng bộ tập tin
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
                case "GET_SHARED_USER" -> {
                    // Lấy danh sách người dùng đã được chia sẻ đối với dữ liệu nào đó
                }
                case "SEARCH_UNSHARED_USER" -> {
                    // Danh sách người dùng chưa được chia sẻ dữ liệu nào đó
                }
                case "SHARE" -> {
                    // Chia sẻ dữ liệu cho người dùng khác
                }
                case "CHECK_USER_PERMISSION" -> {
                    // Kiểm tra quyền hạn của người dùng đối với tập dữ liệu nào đó
                    // Nhận tham số do Client gửi và chuyển về đúng định dạng
                    int response = new PermissionService().checkUserPermission(userId, itemId, isFolder);
                    sendResponse(response);
                }
                case "GET_PUBLIC_PERMISSION" -> {
                    // Lấy quyền truy cập công khai đối với tập dữ liệu nào đó
                    // Nhận tham số do Client gửi và chuyển về đúng định dạng
                    int response = new PermissionService().getPublicPermission(itemId, isFolder);
                    sendResponse(response);
                }
                case "DELETE" -> {
                    // Tiến hành xóa mềm đối với dữ liệu, chuyển dữ liệu sang “Thùng rác” lưu trữ
                    // Nhận tham số do Client gửi và chuyển về đúng định dạng
                    // Kiểm tra quyền XÓA, nếu không có quyền thì báo lỗi cho Client
                    // Tiền hành chuyển dữ liệu vào thùng rác
                }
                case "DELETE_PERMANENTLY" -> {
                    // Xóa dữ liệu vĩnh viễn
                    // Nhận tham số do Client gửi và chuyển về đúng định dạng
                    // Xóa dữ liệu trong cơ sở dữ liệu
                    // Nếu thành công, xóa tập dữ liệu trong hệ thống
                }
                case "RESTORE" -> {
                    //  Khôi mục lại những dữ liệu được lưu trong “Thùng rác”
                    // Nhận tham số do Client gửi và chuyển về đúng định dạng
                    // Tiền hành khôi phục trong cơ sở dữ liệu
                    // Sau đó di chuyển dữ liệu từ Thùng rác về địa chỉ ban đầu
                    // Gửi phản hồi
                }
            }
            // Các trường hợp xử lý khác ...
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // Cuối cùng, đóng kết nối các luồng vào ra và kết nối Socket. Lưu thông tin truy cập của Client
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
                addConnection("DISCONNECTED");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendResponse (Object response){
            try {
                out.writeObject(response);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Object receiveRequest () throws IOException, ClassNotFoundException {
            return in.readObject();
        }
    }
}


        public void addConnection(String request) {
            Connection connection = new Connection(clientAddress.getHostAddress(), request);
            connections.add(connection);
//        System.out.println("Connection added: " + connection);
//        System.out.println("Connection list: " + connections);
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
                folderService.deleteFolderIfExist(folderPath);
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

        private String getFileChanged(int fileId, String fileName) {
            FileService fileService = new FileService(); // Ensure FileService is initialized correctly
            String rs = fileService.getFilePathChanged(fileId, fileName);
            return rs;
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

        private User getUserById(int id) {
            UserService userService = new UserService();
            System.out.println("Get user by id");
            return userService.getUserById(id);
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


    }
}
