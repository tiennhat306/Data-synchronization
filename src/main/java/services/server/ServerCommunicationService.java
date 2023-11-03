package services.server;

import DTO.Connection;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerCommunicationService {
    private ServerSocket serverSocket;
    public static List<Connection> connections = new ArrayList<>();
    private static boolean isRunning = false;

    public ServerCommunicationService(int port) {
        try {
            serverSocket = new ServerSocket(port);
            InetAddress address = InetAddress.getLocalHost();
            System.out.println("TCP/Server running on: " + address + ", Port: " + serverSocket.getLocalPort());
            ServerCommunicationService.isRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerCommunicationService(){
        this(6969);
    }


    public void startListening() {
        Thread thread = new Thread(() -> {
            try{
                while (ServerCommunicationService.isRunning) {
                    Socket clientSocket = serverSocket.accept();

                    try (clientSocket) {
                        System.out.println("New client connected: " + clientSocket.getInetAddress());
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        System.out.println("Client handler connected: " + clientHandler);
                        System.out.println("Client handler added to connections: " + clientHandler.getConnection());
                        connections.add(clientHandler.getConnection());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(serverSocket != null){
                        serverSocket.close();
                        System.out.println("Server socket closed");
                    }
                } catch (IOException e) {
                    System.err.println("Could not close server socket");
                    System.exit(1);
                }
            }
        });
        thread.start();
    }

    public void stopListening() {
        try {
            ServerCommunicationService.isRunning = false;
            if(serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Connection> getAllConnection() {
        return connections;
    }
}