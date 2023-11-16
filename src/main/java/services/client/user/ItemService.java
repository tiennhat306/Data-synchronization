package services.client.user;

import models.File;
import services.client.SocketClientHelper;

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

            socketClientHelper.sendFile(fileName, ownerId, folderId, size, filePath);

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

            socketClientHelper.sendFolder(folderName, ownerId, parentId, folderPath);

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

            String userPath = (String) socketClientHelper.receiveResponse();
            System.out.println("userPath: " + userPath);
            socketClientHelper.syncFolder(userPath);

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
