package services.client.user;

import services.client.SocketClientHelper;

public class PermissionService {
    public int checkPermission(int userId, int id, boolean isFolder) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("CHECK_USER_PERMISSION");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(id));
            socketClientHelper.sendRequest(String.valueOf(isFolder));

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updatePublicPermission(int itemId, boolean isFolder, int finalPermissionId) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("UPDATE_PUBLIC_PERMISSION");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));
            socketClientHelper.sendRequest(String.valueOf(finalPermissionId));

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Integer getPublicPermission(int itemId, boolean isFolder) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_PUBLIC_PERMISSION");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getOwnerId(int itemId, boolean isFolder) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_OWNER_ID");
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(isFolder));

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
