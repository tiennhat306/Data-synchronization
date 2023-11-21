package services.client.user;

import models.File;
import services.client.SocketClientHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ItemService {
    public ItemService() {
    }

    public List<File> getAllItem(int folderId){
        try {
            while(true){
                SocketClientHelper socketClientHelper = new SocketClientHelper();
                // send request to server
                socketClientHelper.sendRequest("GET_ALL_ITEM");
                socketClientHelper.sendRequest(String.valueOf(folderId));

                Object obj = socketClientHelper.receiveResponse();
                List<File> itemList = (List<File>) obj;

                socketClientHelper.close();
                return itemList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createFolder(String folderName, int ownerId, int folderId){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("CREATE_FOLDER");
            socketClientHelper.sendRequest(folderName);
            // send owner id
            socketClientHelper.sendRequest(String.valueOf(ownerId));
            // send current folder Id to server to create new folder
            socketClientHelper.sendRequest(String.valueOf(folderId));

            Object obj = socketClientHelper.receiveResponse();
            boolean response = (boolean) obj;

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean uploadFile(String fileName, int ownerId, int folderId, int size, String filePath){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("UPLOAD_FILE");

            socketClientHelper.sendRequest("file");
            socketClientHelper.sendRequest(fileName);
            socketClientHelper.sendRequest(String.valueOf(ownerId));
            socketClientHelper.sendRequest(String.valueOf(folderId));
            socketClientHelper.sendRequest(String.valueOf(size));

            socketClientHelper.sendFile(size, filePath);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            System.out.println("Response: " + response);
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean uploadFolder(String folderName, int ownerId, int parentId, String folderPath){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("UPLOAD_FOLDER");

            socketClientHelper.sendRequest("folder");
            socketClientHelper.sendRequest(folderName);
            socketClientHelper.sendRequest(String.valueOf(ownerId));
            socketClientHelper.sendRequest(String.valueOf(parentId));

            socketClientHelper.sendFolder(ownerId, folderPath);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            System.out.println("Response: " + response);

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean synchronize(int userId, int currentFolderId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("SYNCHRONIZE");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(currentFolderId));

            String folderPath = (String) socketClientHelper.receiveResponse();
            System.out.println("folderPath: " + folderPath);

            deleteFolderIfExist(folderPath);
            Files.createDirectories(Paths.get(folderPath));

            socketClientHelper.syncFolder(folderPath);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            System.out.println("Response: " + response);
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteFolderIfExist(String folderPath) throws IOException {
        java.io.File folder = new java.io.File(folderPath);
        if(folder.exists()){
            java.io.File[] files = folder.listFiles();
            if(files != null){
                for(java.io.File file : files){
                    if(file.isDirectory()){
                        deleteFolderIfExist(file.getAbsolutePath());
                    } else {
                        Files.deleteIfExists(file.toPath());
                    }
                }
            }
        }
        Files.deleteIfExists(folder.toPath());
    }

    public boolean downloadFolder(String absolutePath, int currentFolderId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("DOWNLOAD_FOLDER");
            socketClientHelper.sendRequest(String.valueOf(currentFolderId));

            String folderName = (String) socketClientHelper.receiveResponse();
            System.out.println("folderName: " + folderName);
            int size = Integer.parseInt((String) socketClientHelper.receiveResponse());

            socketClientHelper.downloadFolder(absolutePath + java.io.File.separator + folderName, size);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            System.out.println("Response: " + response);
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
