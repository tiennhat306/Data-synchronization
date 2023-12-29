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
}

