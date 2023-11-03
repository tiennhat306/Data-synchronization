package services.server;

import DTO.Connection;
import DTO.Item;
import models.User;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import services.server.admin.UserService;
import services.server.user.ItemService;

public class ClientHandler extends Thread{
    private Socket clientSocket;
    private ObjectInputStream in;
    private InetAddress clientAddress;

    public ClientHandler(Socket clientSocket) {
        try{
            this.clientSocket = clientSocket;
            clientAddress = clientSocket.getInetAddress();
            System.out.println("Client handler connected: " + clientSocket);
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Object obj = in.readObject();
            String request = "";
            if(obj instanceof String) {
                request = (String) obj;
                System.out.println("Client request: " + request);
            }
            else {
                System.out.println("Unknown request: " + obj);
            }
            switch (request) {
                case "GET_ALL_USER" -> {
                    List<User> response = getUserList();
                    System.out.println(response);
                    sendResponse(response);
                    //out.println(userListJson);
                }
                case "GET_USER_BY_ID" -> {
                    //out.println(userJson);
                }
                case "GET_ALL_ITEM" -> {

                    //out.println(itemListJson);
                }
                default -> {
                    System.out.println("Unknown request: " + request);
                    //out.println("Unknown request: " + request);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in != null) in.close();
                if(clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<User> getUserList() {
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

    public void sendResponse(List<User> response) {
        Socket responseSocket = null;
        ObjectOutputStream responseOut = null;
        try{
            responseSocket = new Socket(clientAddress, 9696);
            responseOut = new ObjectOutputStream(responseSocket.getOutputStream());
            responseOut.writeObject(response);
            responseOut.flush();
            responseOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(responseOut != null) responseOut.close();
                if(responseSocket != null) responseSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Object receiveRequest() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    public Connection getConnection() {
        return new Connection(clientAddress.toString(), clientSocket.getPort());
    }
}
