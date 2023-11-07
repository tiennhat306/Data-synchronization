package services.server;

import DTO.Connection;
import models.File;
import models.User;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import services.server.admin.UserService;
import services.server.user.FileService;
import services.server.user.FolderService;
import services.server.user.ItemService;

import static applications.ServerApp.connections;

public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private int clientNumber;
    private ObjectInputStream in;
    private InetAddress clientAddress;
    //CountDownLatch ioReady = new CountDownLatch(1); // Initialize with 1

    public ClientHandler(Socket clientSocket, int clientNumber) {
        try{
            this.clientSocket = clientSocket;
            this.clientNumber = clientNumber;
            clientAddress = clientSocket.getInetAddress();
            System.out.println("Client handler connected: " + clientSocket);
            System.out.println("Server thread number " + clientNumber + " Started");
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

                        sendResponse(response);
                    } else {
                        System.out.println("Unknown request: " + type);
                    }
                }
                case "UPLOAD_FOLDER" -> {
//                    String folderName = (String) receiveRequest();
//                    int ownerId = Integer.parseInt((String) receiveRequest());
//                    int currentFolderId = Integer.parseInt((String) receiveRequest());
//                    String rs = uploadFolder(folderName, ownerId, currentFolderId);
                    String type_response = (String) receiveRequest();
                    boolean response = false;
                    while(!type_response.equals("END")){
                        response = uploadFolder(type_response);
                        type_response = (String) receiveRequest();
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
                if(in != null) in.close();
                if(clientSocket != null) clientSocket.close();
                addConnection("DISCONNECTED");
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    
    private boolean uploadFile(String fileName, int ownerId, int folderId, int size) throws IOException {
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
            System.out.println("Thêm file " + fileName + " thành công");
            receiveFile(rs);
            response = true;
        } else {
            System.out.println("Thêm file " + fileName + " thất bại");
        }
        System.out.println("Response: " + response);
        return response;
    }

    private void receiveFile(String filePath){
        try{
            // Đọc dữ liệu tệp
            InputStream fileInputStream = clientSocket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private boolean uploadFolder(String type_request) throws IOException, ClassNotFoundException {
        String type = type_request;
        if(type.equals("folder")){
            System.out.println("Upload folder");

            String folderName = (String) receiveRequest();
            int ownerId = Integer.parseInt((String) receiveRequest());
            int parentId = Integer.parseInt((String) receiveRequest());

            FolderService folderService = new FolderService();
            int rs = folderService.uploadFolder(folderName, ownerId, parentId);
            if(rs != -1){
                System.out.println("Thêm folder "+ folderName + " thành công");
            } else {
                System.out.println("Thêm folder "+ folderName + " thất bại");
            }
            // send folder id to client
            sendResponse(String.valueOf(rs));

            return rs!=-1;
        } else if(type.equals("file")) {
            System.out.println("Upload file");

            String fileName = (String) receiveRequest();
            int ownerId = Integer.parseInt((String) receiveRequest());
            int parentId = Integer.parseInt((String) receiveRequest());
            int size = Integer.parseInt((String) receiveRequest());

            boolean response = uploadFile(fileName, ownerId, parentId, size);
//            FolderService folderService = new FolderService();
//            boolean rs =  folderService.uploadFolder(folderName, ownerId, parentId);
            if(response){
                System.out.println("Thêm file "+ fileName + " thành công");
            } else {
                System.out.println("Thêm file "+ fileName + " thất bại");
            }
            return response;
        }

        return true;
    }
    
    private User getUserById(int id) {
        UserService userService = new UserService();
        System.out.println("Get user by id");
        return userService.getUserById(id);
    }

    public void sendResponse(Object response) {
        Socket responseSocket = null;
        ObjectOutputStream responseOut = null;
        try{
            responseSocket = new Socket(clientAddress, 9696);
            responseOut = new ObjectOutputStream(responseSocket.getOutputStream());
            responseOut.writeObject(response);
            responseOut.flush();
            responseOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(responseOut != null) responseOut.close();
                if(responseSocket != null) responseSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
