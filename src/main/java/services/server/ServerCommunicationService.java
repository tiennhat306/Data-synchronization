package services.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerCommunicationService {
    private ServerSocket serverSocket;
    private int noOfThreads = 0;
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
        ServerCommunicationService.isRunning = true;
        Thread thread = new Thread(() -> {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(10, // corePoolSize
                    100, // maximumPoolSize
                    10, // thread timeout
                    TimeUnit.SECONDS, new ArrayBlockingQueue<>(8) // queueCapacity
            );
            try{
                while (ServerCommunicationService.isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(10000);
                    //clientSocket.setSoLinger(true, 5000);
                    try {
                        System.out.println("New client connected: " + clientSocket.getInetAddress());
                        int dataPort = 6969 + noOfThreads + 1;
                        System.out.println(dataPort);
                        // Create new worker thread for new connection
                        //Worker w = new Worker(clientSocket, dataPort);
                        System.out.println("New connection received. Worker was created.");
                        //w.start();
                        ClientHandler clientHandler = new ClientHandler(clientSocket, noOfThreads++);
                        System.out.println("Client handler connected: " + clientHandler);
                        ////clientHandler.join();
                        executor.execute(clientHandler);
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
}
