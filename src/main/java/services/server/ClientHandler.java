package services.server;

import DTO.Connection;
import DTO.Item;
import DTO.UserData;
import models.User;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.Gson;
import services.server.admin.UserService;
import services.server.user.ItemService;

public class ClientHandler extends Thread{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    Gson gson;

    public ClientHandler(Socket clientSocket) {
        try{
            this.clientSocket = clientSocket;
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
            gson = new Gson();
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Process request
            System.out.println("Client handler connected: " + clientSocket.getInetAddress());
            String request = in.readLine();
            System.out.println("Client request: " + request);
            switch (request) {
                case "GET_ALL_USER" -> {
                    List<UserData> userList = getUserList();
                    List<LinkedHashMap<String, Object>> userLists = new ArrayList<>();
                    for (UserData user : userList) {
                        LinkedHashMap<String, Object> userDataToJson = user.getUserData();
                        userLists.add(userDataToJson);
                    }
                    String userListJson = gson.toJson(userLists);
                    System.out.println("userListJson" + userListJson);
                    out.println(userListJson);
                }
                case "GET_USER_BY_ID" -> {
                    int id = Integer.parseInt(in.readLine());
                    String userJson = gson.toJson(getUserById(id));
                    System.out.println(userJson);
                    out.println(userJson);
                    out.flush();
                }
                case "GET_ALL_ITEM" -> {
                    int folderId = Integer.parseInt(in.readLine());
                    String itemListJson = gson.toJson(getItemList(folderId));
                    System.out.println(itemListJson);
                    out.println(itemListJson);
                    out.flush();
                }
                default -> {
                    System.out.println("Unknown request: " + request);
                    out.println("Unknown request: " + request);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        finally {
//            try {
//                if(in != null) in.close();
//                if(out != null) out.close();
//                if(clientSocket != null) clientSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private List<UserData> getUserList() {
        UserService userService = new UserService();
        System.out.println("Get all user");
        return userService.getAllUser();
    }
    private List<Item> getItemList(int folderId) {
        ItemService itemService = new ItemService();
        System.out.println("Get all item");
        return itemService.getAllItem(folderId);
    }
    private User getUserById(int id) {
        UserService userService = new UserService();
        System.out.println("Get user by id");
        return userService.getUserById(id);
    }
//    private String processClientRequest(String request) {
//        // Xử lý yêu cầu từ Client ở đây
//        return "Server response to client request: " + request;
//    }

    public Connection getConnection() {
        return new Connection(clientSocket.getInetAddress().toString(), clientSocket.getPort());
    }
}
