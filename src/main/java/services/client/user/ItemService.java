package services.client.user;

import models.File;
import models.User;
import services.client.SocketClientHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    public ItemService() {
    }

    public List<File> getAllItem(int userId, int folderId, String searchText){
        try {
            while(true){
                SocketClientHelper socketClientHelper = new SocketClientHelper();
                // send request to server
                socketClientHelper.sendRequest("GET_ALL_ITEM");
                socketClientHelper.sendRequest(String.valueOf(userId));
                socketClientHelper.sendRequest(String.valueOf(folderId));
                socketClientHelper.sendRequest(searchText);

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
    public List<File> getAllItemPrivateOwnerId(int ownerId, String searchText) {
        try {
            while (true) {
                SocketClientHelper socketClientHelper = new SocketClientHelper();
                // send request to server
                socketClientHelper.sendRequest("GET_ALL_ITEM_PRIVATE");
                socketClientHelper.sendRequest(String.valueOf(ownerId));
                socketClientHelper.sendRequest(searchText);

                Object obj = socketClientHelper.receiveResponse();
                List<File> itemList = (List<File>) obj;
                return itemList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<File> getAllOtherShareItem(int ownerId, String searchText){
        try {
            while (true) {
                SocketClientHelper socketClientHelper = new SocketClientHelper();
                // send request to server
                socketClientHelper.sendRequest("GET_ALL_ITEM_OSHARE");
                socketClientHelper.sendRequest(String.valueOf(ownerId));
                socketClientHelper.sendRequest(searchText);

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
    public List<File> getAllSharedItem(int ownerId, String searchText){
        try {
            while (true) {
                SocketClientHelper socketClientHelper = new SocketClientHelper();
                // send request to server
                socketClientHelper.sendRequest("GET_ALL_ITEM_SHARED");
                socketClientHelper.sendRequest(String.valueOf(ownerId));
                socketClientHelper.sendRequest(searchText);

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

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean synchronizeFolder(int userId,  int currentFolderId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("SYNCHRONIZE_FOLDER");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(currentFolderId));

            String folderPath = (String) socketClientHelper.receiveResponse();

            deleteFolderIfExist(folderPath);
            Files.createDirectories(Paths.get(folderPath));

            socketClientHelper.syncFolder(folderPath);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean synchronizeFile(int userId, int fileId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("SYNCHRONIZE_FILE");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(fileId));

            String filePath = (String) socketClientHelper.receiveResponse();
            int size = Integer.parseInt((String)socketClientHelper.receiveResponse());

            Files.deleteIfExists(Paths.get(filePath));
            java.io.File file = new java.io.File(filePath);
            java.io.File parent = file.getParentFile();
            if(!parent.exists()){
                parent.mkdirs();
            }
            file.createNewFile();


            socketClientHelper.syncFile(filePath, size);

            boolean response = (boolean) socketClientHelper.receiveResponse();
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

    public boolean downloadFolder(String absolutePath, int userId, int currentFolderId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("DOWNLOAD_FOLDER");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(currentFolderId));

            String folderName = (String) socketClientHelper.receiveResponse();
            int size = Integer.parseInt((String)socketClientHelper.receiveResponse());

            socketClientHelper.downloadFolder(absolutePath + java.io.File.separator + folderName, size);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> searchUnsharedUser(int itemTypeId, int itemId, String keyword) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("SEARCH_UNSHARED_USER");
            socketClientHelper.sendRequest(String.valueOf(itemTypeId));
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(keyword);

            Object obj = socketClientHelper.receiveResponse();
            List<User> userList = (List<User>) obj;

            socketClientHelper.close();
            return userList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean share(int itemTypeId, int itemId, int permissionId, int sharedBy, ArrayList<Integer> userIds) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("SHARE");
            socketClientHelper.sendRequest(String.valueOf(itemTypeId));
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(permissionId));
            socketClientHelper.sendRequest(String.valueOf(sharedBy));
            socketClientHelper.sendRequest(String.valueOf(userIds.size()));
            for(int userId : userIds){
                socketClientHelper.sendRequest(String.valueOf(userId));
            }

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getSharedUser(int itemTypeId, int itemId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_SHARED_USER");
            socketClientHelper.sendRequest(String.valueOf(itemTypeId));
            socketClientHelper.sendRequest(String.valueOf(itemId));

            Object obj = socketClientHelper.receiveResponse();
            List<User> userList = (List<User>) obj;

            socketClientHelper.close();
            return userList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteItem(int itemTypeId, int itemId, int userId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("DELETE");
            socketClientHelper.sendRequest(String.valueOf(itemTypeId));
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(userId));

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItemPermanently(int itemTypeId, int itemId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("DELETE_PERMANENTLY");
            socketClientHelper.sendRequest(String.valueOf(itemTypeId));
            socketClientHelper.sendRequest(String.valueOf(itemId));

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean downloadFile(String path, int itemId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("DOWNLOAD_FILE");
            socketClientHelper.sendRequest(String.valueOf(itemId));

            String fileName = (String) socketClientHelper.receiveResponse();
            int size = Integer.parseInt((String)socketClientHelper.receiveResponse());

            socketClientHelper.downloadFile(path + java.io.File.separator + fileName, size);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restore(int itemTypeId, int itemId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("RESTORE");
            socketClientHelper.sendRequest(String.valueOf(itemTypeId));
            socketClientHelper.sendRequest(String.valueOf(itemId));

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<File> getAllDeletedItem(int userId, String txt) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_ALL_DELETED_ITEM");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(txt);

            Object obj = socketClientHelper.receiveResponse();
            List<File> itemList = (List<File>) obj;

            socketClientHelper.close();
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean openFolder(int userId, int folderId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("OPEN_FOLDER");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(folderId));

            String folderPath = (String) socketClientHelper.receiveResponse();

            deleteFolderIfExist(folderPath);
            Files.createDirectories(Paths.get(folderPath));

            socketClientHelper.syncFolder(folderPath);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean openFile(int userId, int fileId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("OPEN_FILE");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(fileId));

            String filePath = (String) socketClientHelper.receiveResponse();
            int size = Integer.parseInt((String)socketClientHelper.receiveResponse());

            Files.deleteIfExists(Paths.get(filePath));
            java.io.File file = new java.io.File(filePath);
            java.io.File parent = file.getParentFile();
            if(!parent.exists()){
                parent.mkdirs();
            }
            file.createNewFile();

            socketClientHelper.syncFile(filePath, size);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
