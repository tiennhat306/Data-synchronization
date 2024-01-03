package services.client.user;

import services.client.SocketClientHelper;

public class UserService {
    public String getUserPath(int userId) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_USER_PATH");
            socketClientHelper.sendRequest(String.valueOf(userId));

            String response = (String) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
