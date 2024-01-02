package services.client.user;

import DTO.*;
import enums.UploadStatus;
import services.client.SocketClientHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    public ItemService() {
    }

    public List<ItemDTO> getAllItem(int userId, int folderId, String searchText){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_ALL_ITEM");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(folderId));
            socketClientHelper.sendRequest(searchText);

            Object obj = socketClientHelper.receiveResponse();
            List<ItemDTO> itemList = (List<ItemDTO>) obj;

            socketClientHelper.close();
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<MoveCopyFolderDTO> getAllFolderPopUps(int userId, int itemId, boolean isFolder, int folderId){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_ALL_ITEM_POPS_UP");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(String.valueOf(folderId));

            Object obj = socketClientHelper.receiveResponse();
            List<MoveCopyFolderDTO> itemList = (List<MoveCopyFolderDTO>) obj;

            socketClientHelper.close();
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ItemDTO> getAllItemPrivateOwnerId(int ownerId, String searchText) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_ALL_ITEM_PRIVATE");
            socketClientHelper.sendRequest(String.valueOf(ownerId));
            socketClientHelper.sendRequest(searchText);

            Object obj = socketClientHelper.receiveResponse();
            List<ItemDTO> itemList = (List<ItemDTO>) obj;
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<ItemDTO> getAllOtherShareItem(int ownerId, String searchText){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_ALL_ITEM_OSHARE");
            socketClientHelper.sendRequest(String.valueOf(ownerId));
            socketClientHelper.sendRequest(searchText);

            Object obj = socketClientHelper.receiveResponse();
            List<ItemDTO> itemList = (List<ItemDTO>) obj;

            socketClientHelper.close();
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<ItemDTO> getAllSharedItem(int ownerId, String searchText){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_ALL_ITEM_SHARED");
            socketClientHelper.sendRequest(String.valueOf(ownerId));
            socketClientHelper.sendRequest(searchText);

            Object obj = socketClientHelper.receiveResponse();
            List<ItemDTO> itemList = (List<ItemDTO>) obj;

            socketClientHelper.close();
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<RecentFileDTO> getAllRecentOpenedItem(int userId, String searchText) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_ALL_RECENT_OPENED_ITEM");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(searchText);

            Object obj = socketClientHelper.receiveResponse();
            List<RecentFileDTO> itemList = (List<RecentFileDTO>) obj;

            socketClientHelper.close();
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int createFolder(String folderName, int ownerId, int folderId, boolean isReplace){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            if(isReplace){
                socketClientHelper.sendRequest("CREATE_FOLDER_AND_REPLACE");
            } else {
                socketClientHelper.sendRequest("CREATE_FOLDER");
            }
            socketClientHelper.sendRequest(folderName);
            socketClientHelper.sendRequest(String.valueOf(ownerId));
            socketClientHelper.sendRequest(String.valueOf(folderId));

            Object obj = socketClientHelper.receiveResponse();
            int response = (int) obj;

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    public int uploadFile(int userId, String fileName, int folderId, int size, String filePath){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("UPLOAD_FILE");

            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(fileName);
            socketClientHelper.sendRequest(String.valueOf(folderId));
            socketClientHelper.sendRequest(String.valueOf(size));

            int checkPermission = (int) socketClientHelper.receiveResponse();
            if(checkPermission != UploadStatus.PERMISSION_ACCEPTED.getValue()){
                return checkPermission;
            }

            socketClientHelper.sendFile(size, filePath);

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    public int uploadFileAndReplace(int userId, String fileName, int currentFolderId, int length, String filePath) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("UPLOAD_FILE_AND_REPLACE");

            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(fileName);
            socketClientHelper.sendRequest(String.valueOf(currentFolderId));
            socketClientHelper.sendRequest(String.valueOf(length));

            int checkPermission = (int) socketClientHelper.receiveResponse();
            if(checkPermission != UploadStatus.PERMISSION_ACCEPTED.getValue()){
                return checkPermission;
            }

            socketClientHelper.sendFile(length, filePath);

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    public int uploadFolder(int userId, String folderName, int parentId, String folderPath){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("UPLOAD_FOLDER");

            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(folderName);
            socketClientHelper.sendRequest(String.valueOf(parentId));

            int checkPermission = (int) socketClientHelper.receiveResponse();
            if(checkPermission != UploadStatus.PERMISSION_ACCEPTED.getValue()){
                return checkPermission;
            }
            socketClientHelper.sendFolder(userId, folderPath);

            int response = (int) socketClientHelper.receiveResponse();

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    public int uploadFolderAndReplace(int userId, String folderName, int currentFolderId, String folderPath) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("UPLOAD_FOLDER_AND_REPLACE");

            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(folderName);
            socketClientHelper.sendRequest(String.valueOf(currentFolderId));

            int checkPermission = (int) socketClientHelper.receiveResponse();
            if(checkPermission != UploadStatus.PERMISSION_ACCEPTED.getValue()){
                return checkPermission;
            }
            socketClientHelper.sendFolder(userId, folderPath);

            int response = (int) socketClientHelper.receiveResponse();

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
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
//                parent.mkdirs();
                Files.createDirectories(Paths.get(parent.getAbsolutePath()));
            }
//            file.createNewFile();
            Files.createFile(Paths.get(file.getAbsolutePath()));

            socketClientHelper.syncFile(filePath, size);

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

    public List<UserToShareDTO> getSharedUser(int itemId, boolean isFolder) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_SHARED_USER");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));

            Object obj = socketClientHelper.receiveResponse();
            List<UserToShareDTO> userSharedList = (List<UserToShareDTO>) obj;

            socketClientHelper.close();
            return userSharedList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteSharedUser(int itemId, boolean isFolder, int userId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("DELETE_SHARED_USER");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(String.valueOf(userId));

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UserToShareDTO> searchUnsharedUser(int itemId, boolean isFolder, String keyword) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("SEARCH_UNSHARED_USER");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(keyword);

            Object obj = socketClientHelper.receiveResponse();
            List<UserToShareDTO> userUnsharedList = (List<UserToShareDTO>) obj;

            socketClientHelper.close();
            return userUnsharedList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateSharePermission(int itemId, boolean isFolder, int permissionType, int ownerId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("UPDATE_SHARE_PERMISSION");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(String.valueOf(permissionType));
            socketClientHelper.sendRequest(String.valueOf(ownerId));

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean share(int itemId, boolean isFolder, int permissionType, int sharedBy, ArrayList<Integer> userIds) {
        if(userIds.isEmpty()) return false;

        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("SHARE");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(String.valueOf(permissionType));
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

    public boolean deleteItem(int itemId, boolean isFolder, int userId) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("DELETE");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(String.valueOf(userId));

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItemPermanently(int itemId, boolean isFolder) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("DELETE_PERMANENTLY");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rename(int userId, int itemID, boolean isFolder, String fileName) {
		try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("RENAME");

            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(itemID));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(fileName);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}

	public int move(int userId, int itemId, boolean isFolder, int targetFolderId, boolean isReplace) {
    	try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            if(isReplace){
                socketClientHelper.sendRequest("MOVE_AND_REPLACE");
            } else {
                socketClientHelper.sendRequest("MOVE");
            }
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(String.valueOf(targetFolderId));

            int response = (int) socketClientHelper.receiveResponse();

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    public int copy(int userId, int itemID, boolean isFolder, int targetFolderId, boolean isReplace){
    	try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            if(isReplace){
                socketClientHelper.sendRequest("COPY_AND_REPLACE");
            } else {
                socketClientHelper.sendRequest("COPY");
            }
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(itemID));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(String.valueOf(targetFolderId));

            int response = (int) socketClientHelper.receiveResponse();

            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    public int restore(int itemId, boolean isFolder) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("RESTORE");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    public int restoreAndReplace(int itemId, boolean isFolder) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("RESTORE_AND_REPLACE");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    public List<ItemDeletedDTO> getAllDeletedItem(int userId, String txt) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_ALL_DELETED_ITEM");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(txt);

            Object obj = socketClientHelper.receiveResponse();
            List<ItemDeletedDTO> itemList = (List<ItemDeletedDTO>) obj;

            socketClientHelper.close();
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String openFile(int userId, int fileId) {
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
//                parent.mkdirs();
                Files.createDirectories(Paths.get(parent.getAbsolutePath()));
            }
//            file.createNewFile();
            Files.createFile(Paths.get(file.getAbsolutePath()));

            socketClientHelper.syncFile(filePath, size);

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            if(response){
                return filePath;
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String openFolder(int userId, int folderId) {
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
            if(response){
                return folderPath;
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
