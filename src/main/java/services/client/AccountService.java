package services.client;

import DTO.UserAccountDTO;

import java.sql.Date;

public class AccountService {
    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("UPDATE_PASSWORD");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(oldPassword);
            socketClientHelper.sendRequest(newPassword);

            boolean result = (boolean) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public UserAccountDTO getUserAccountInfo(int userId) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_USER_ACCOUNT_INFO");
            socketClientHelper.sendRequest(String.valueOf(userId));

            UserAccountDTO userAccountDTO = (UserAccountDTO) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return userAccountDTO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUserInfo(int userId, String username, String name, String email, String phone, Date birth, boolean gender) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("UPDATE_USER");
            socketClientHelper.sendRequest(String.valueOf(userId));
            socketClientHelper.sendRequest(username);
            socketClientHelper.sendRequest(name);
            socketClientHelper.sendRequest(email);
            socketClientHelper.sendRequest(phone);
            socketClientHelper.sendRequest(birth);
            socketClientHelper.sendRequest(gender);

            // receive response from server
            Object obj = socketClientHelper.receiveResponse();
            boolean success = (boolean) obj;
            socketClientHelper.close();
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
