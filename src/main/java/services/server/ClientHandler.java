package services.server;

import DTO.Connection;
import models.File;
import models.User;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import services.server.admin.UserService;
import services.server.user.FileService;
import services.server.user.FolderService;
import services.server.user.ItemService;
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
                System.out.println("Unknown request: " + obj);
            }
            addConnection(request);

            switch (request) {
                case "GET_ALL_USER" -> {
                    List<User> response = getUserList();
                    System.out.println(response);
                    sendResponse(response);
                }
                case "GET_USER_BY_ID" -> {
                }
                case "GET_ALL_ITEM" -> {
                    String folderId = (String) receiveRequest();
                    List<File> response = getItemList(Integer.parseInt(folderId));
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
                        System.out.println("Response of router: " + response);
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
                        boolean response = uploadFolder(folderName, ownerId, currentFolderId);

                        sendResponse(response);
                    } else {
                        System.out.println("Unknown request: " + type_request);
                    }
                }
                case "DOWNLOAD_FILE" -> {
                    int fileId = Integer.parseInt((String) receiveRequest());
                    FileService fileService = new FileService();

                    String filePath = fileService.getFilePath(fileId);
                    System.out.println("filePath: " + filePath);

                    int size = fileService.sizeOfFile(fileId);
                    sendResponse(String.valueOf(size));

                    syncFile(filePath, size);
                }
                case "DOWNLOAD_FOLDER" -> {
                    int folderId = Integer.parseInt((String) receiveRequest());
                    boolean response =  downloadFolder(folderId);
                    sendResponse(response);
                }
                case "SYNCHRONIZE" -> {
                    int userId = Integer.parseInt((String) receiveRequest());
                    int folderId = Integer.parseInt((String) receiveRequest());

                    services.server.user.UserService userService = new services.server.user.UserService();
                    String userPath = userService.getUserPath(userId);

                    services.server.user.FolderService folderService = new services.server.user.FolderService();
                    String path = folderService.getPath(folderId);
                    sendResponse(userPath + java.io.File.separator + path);

                    boolean response = syncFolder(folderService.getFolderPath(folderId));

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

    private boolean downloadFolder(int folderId) {
        try {
            FolderService folderService = new FolderService();
            String folderName = folderService.getFolderName(folderId);
            sendResponse(folderName);

            String folderPath = folderService.getFolderPath(folderId);
            System.out.println("folderPath: " + folderPath);

            ZipFolder zipFolder = new ZipFolder(folderName, folderPath);
            String zipFilePath = zipFolder.zip();
            System.out.println("zipFilePath: " + zipFilePath);

            int size = (int) zipFolder.size();
            sendResponse(String.valueOf(size));

            sendZipFolder(zipFilePath, size);
            zipFolder.deleteOutputZipFile();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendZipFolder(String zipFilePath, int size) {
        byte[] buffer = new byte[1024];

        try(FileInputStream fileInputStream = new FileInputStream(zipFilePath)) {
            OutputStream fileOutputStream = clientSocket.getOutputStream();
            int bytesRead;
            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            fileOutputStream.flush();
            System.out.println("File sent: " + zipFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<User> getUserList() {
        UserService userService = new UserService();
        System.out.println("Get all user");
        return userService.getAllUser();
    }
    private List<File> getItemList(int folderId) {
        ItemService itemService = new ItemService();
        System.out.println("Get all item");
        return itemService.getAllItem(folderId);
    }
    
    private boolean uploadFile(String fileName, int ownerId, int folderId, int size){
        try{
            int indexOfDot = fileName.indexOf(".");
            String nameOfFile = fileName.substring(0, indexOfDot); // Characters before the first period
            String typeOfFile = fileName.substring(indexOfDot + 1); // Characters after the first period
            // Send the two parts to the server
            System.out.println(nameOfFile);
            System.out.println(typeOfFile);
            FileService fileService = new FileService();
            String rs = fileService.uploadFile(nameOfFile, fileService.getFileTypeId(typeOfFile), folderId, ownerId, size);
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
            System.out.println("Response: " + response);
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

            System.out.println("File uploaded: " + filePath);
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
            System.out.println("File synchronized: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean syncFolder(String folderPath){
        java.io.File folder = new java.io.File(folderPath);
        java.io.File[] listOfFiles = folder.listFiles();
        try {
            if(listOfFiles != null){
                for (java.io.File file : listOfFiles) {
                    if (file.isDirectory()) {
                        String folderName = file.getName();
                        sendResponse("folder");
                        sendResponse(folderName);
                        syncFolder(file.getAbsolutePath());
                    } else if (file.isFile()) {
                        String fileName = file.getName();
                        String filePath = file.getAbsolutePath();
                        int size = (int) file.length();
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
    
    private boolean uploadFolder(String folderName, int ownerId, int parentId) throws IOException, ClassNotFoundException {
        System.out.println("Upload folder");

        FolderService folderService = new FolderService();
        int rs = folderService.uploadFolder(folderName, ownerId, parentId);
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
                response = uploadFolder(folderNameOfChild, ownerIdOfChild, parentIdOfChild);
                if(!response) check = false;
            } else if(child_type.equals("file")){
                System.out.println("Upload file");

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
        System.out.println("Connection added: " + connection);
        System.out.println("Connection list: " + connections);
    }
}
