package services.server;

import DTO.Connection;
import DTO.Item;
import DTO.UserData;
import com.google.gson.reflect.TypeToken;
import models.User;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.Gson;
import services.server.admin.UserService;
import services.server.user.ItemService;

public class ClientHandler extends Thread{
    private Socket clientSocket;
    //private PrintWriter out;
    private BufferedReader in;
    private Gson gson;
    private String clientAddress;

    public ClientHandler(Socket clientSocket) {
        try{
            this.clientSocket = clientSocket;
            clientAddress = clientSocket.getInetAddress().getHostAddress();
            System.out.println("Client handler connected: " + clientAddress);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
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
            String request = receiveRequest();
            System.out.println("Client request: " + request);
            String response = "";
            switch (request) {
                case "GET_ALL_USER" -> {
                    response = getUserList();
                    System.out.println(response);
                    sendResponse(response);
                    //out.println(userListJson);
                }
                case "GET_USER_BY_ID" -> {
                    int id = Integer.parseInt(in.readLine());
                    String userJson = gson.toJson(getUserById(id));
                    System.out.println(userJson);
                    //out.println(userJson);
                }
                case "GET_ALL_ITEM" -> {
                    int folderId = Integer.parseInt(in.readLine());
                    String itemListJson = gson.toJson(getItemList(folderId));
                    System.out.println(itemListJson);

                    //out.println(itemListJson);
                }
                default -> {
                    System.out.println("Unknown request: " + request);
                    //out.println("Unknown request: " + request);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in != null) in.close();
                //if(out != null) out.close();
                if(clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getUserList() {
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

    public void sendResponse(String request) {
        try(Socket responseSocket = new Socket(clientAddress, 9696)){
            PrintWriter responseOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(responseSocket.getOutputStream())), true);
            responseOut.println(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendObject(Object obj) throws IOException {
        String json = gson.toJson(obj);
        sendResponse(json);
    }

    public String receiveRequest() throws IOException {
        return in.readLine();
    }

    public <T> T receiveObject(Type clazz) throws IOException {
        String json = receiveRequest();
        return gson.fromJson(json, clazz);
    }

    public List<LinkedHashMap<String, Object>> receiveLinkedHashMapList() throws IOException {
        Type type = new TypeToken<List<LinkedHashMap<String, Object>>>(){}.getType();
        String json = receiveRequest();
        return gson.fromJson(json, type);
    }

    public Connection getConnection() {
        return new Connection(clientAddress, clientSocket.getPort());
    }
}
