package services.client.admin;

import models.User;
import services.client.SocketClientHelper;

import java.sql.Date;
import java.util.List;


public class UserService {
    public UserService() {
    }
    public List<User> getAllUser() {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_ALL_USER");

            Object obj = socketClientHelper.receiveResponse();
            List<User> userList = (List<User>) obj;
            socketClientHelper.close();
            return userList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public User getUserByUserName(String username) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_USER_BY_USERNAME");
            socketClientHelper.sendRequest(username);

            // receive response from server
            Object obj = socketClientHelper.receiveResponse();
            User user = (User) obj;
            socketClientHelper.close();
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUser(String username, String name, String email, String phone, Date birth, boolean gender) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("UPDATE_USER");
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
    public boolean changePass(String username, String newPass) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("CHANGE_PASS_USER");
            socketClientHelper.sendRequest(username);
            socketClientHelper.sendRequest(newPass);
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

