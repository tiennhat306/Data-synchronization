package services.client;

import com.google.gson.Gson;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class SocketClientHelper {
    private Socket socket;
    private ObjectOutputStream out;

    public SocketClientHelper(String host, int port) {
        try{
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SocketClientHelper() throws UnknownHostException {
        this(InetAddress.getLocalHost().getHostAddress(), 6969);
    }

    public void close() {
        try {
            if(out != null) out.close();
            if(socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(Object request) throws IOException {
        System.out.println("Sending request: " + request);
        out.writeObject(request);
    }


    public Object receiveResponse(){
        try(ServerSocket responseSocket = new ServerSocket(9696)){
            Socket responseClientSocket = responseSocket.accept();
            ObjectInputStream in = new ObjectInputStream(responseClientSocket.getInputStream());
            Object response = in.readObject();

            if(in != null) in.close();
            if(responseClientSocket != null) responseClientSocket.close();

            return response;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
