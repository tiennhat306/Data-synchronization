package services.client.user;

import services.client.SocketClientHelper;

public class PermissionService {
    public int checkPermission(int userId, int typeId, int id) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("CHECK_PERMISSION");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(String.valueOf(typeId));
            socketClientHelper.sendRequest(String.valueOf(id));

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updatePermission(int itemTypeId, int itemId, int finalPermissionId) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("UPDATE_PERMISSION");
            socketClientHelper.sendRequest(String.valueOf(itemTypeId));
            socketClientHelper.sendRequest(String.valueOf(itemId));
            socketClientHelper.sendRequest(String.valueOf(finalPermissionId));

            boolean response = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Integer getPermission(int itemTypeId, int itemId) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_PERMISSION");
            socketClientHelper.sendRequest(String.valueOf(itemTypeId));
            socketClientHelper.sendRequest(String.valueOf(itemId));

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    public int getOwnerId(int itemTypeId, int itemId) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_OWNER_ID");
            socketClientHelper.sendRequest(String.valueOf(itemTypeId));
            socketClientHelper.sendRequest(String.valueOf(itemId));

            int response = (int) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
