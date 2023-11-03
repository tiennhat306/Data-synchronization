package services.client.admin;

import models.User;
import services.client.SocketClientHelper;
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
//    public User getUserById(int id) {
//        try {
//            SocketClientHelper socketClientHelper = new SocketClientHelper();
//            // send request to server
//            socketClientHelper.sendRequest("GET_USER_BY_ID");
//            socketClientHelper.sendRequest(String.valueOf(id));
//
//            // receive response from server
//            User user = socketClientHelper.receiveObject(User.class);
//            socketClientHelper.close();
//            return user;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}

