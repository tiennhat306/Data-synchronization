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
//            socketClientHelper.sendRequest(fileName);
//            // send owner
//            socketClientHelper.sendRequest(String.valueOf(ownerId));
//            // send current folder Id to server to create new folder
//            socketClientHelper.sendRequest(String.valueOf(folderId));
//            // send file size
//            socketClientHelper.sendRequest(String.valueOf(size));
            // send file

            boolean response = socketClientHelper.sendFile(fileName, ownerId, folderId, size, filePath);

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean uploadFolder(String folderName, int ownerId, int folderId, String folderPath){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("UPLOAD_FOLDER");
//            socketClientHelper.sendRequest(folderName);
//            socketClientHelper.sendRequest(String.valueOf(ownerId));
//            socketClientHelper.sendRequest(String.valueOf(folderId));

//            java.io.File folder = new java.io.File(folderPath);
            socketClientHelper.sendFolder(folderName, ownerId, folderId, folderPath);
            socketClientHelper.sendRequest("END");

            Object obj = socketClientHelper.receiveResponse();
            boolean response = (boolean) obj;

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



}
