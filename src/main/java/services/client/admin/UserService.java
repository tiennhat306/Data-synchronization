package services.client.admin;

import DTO.UserDTO;
import services.client.SocketClientHelper;

import java.util.List;


public class UserService {
    public UserService() {
    }
    public List<UserDTO> getAllUser() {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_ALL_USER");

            Object obj = socketClientHelper.receiveResponse();
            List<UserDTO> userList = (List<UserDTO>) obj;
            socketClientHelper.close();
            return userList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

