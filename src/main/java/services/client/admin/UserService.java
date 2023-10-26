package services.client.admin;

import DTO.UserData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.User;
import org.hibernate.Session;
import services.client.SocketClientHelper;
import utils.HibernateUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class UserService {
    public UserService() {
    }
    public List<UserData> getAllUser() {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("GET_ALL_USER");

            List<LinkedHashMap<String, Object>> userListFromJson = socketClientHelper.receiveLinkedHashMapList();
            System.out.println("userListFromJson: " + userListFromJson);
            List<UserData> userList = new ArrayList<>();
            for(LinkedHashMap<String, Object> userData : userListFromJson) {
                userList.add(new UserData(userData));
            }
            System.out.println("userList from Service: " + userList);
            socketClientHelper.close();
            return userList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public User getUserById(int id) {
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_USER_BY_ID");
            socketClientHelper.sendRequest(String.valueOf(id));

            // receive response from server
            User user = socketClientHelper.receiveObject(User.class);
            socketClientHelper.close();
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

