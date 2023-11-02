package services.client;

import applications.Server;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;


public class SocketClientHelper {
    private Socket socket;
    private PrintWriter out;
    //private BufferedReader in;
    private Gson gson;

    public SocketClientHelper(String host, int port) {
        try{
            socket = new Socket(host, port);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            gson = new Gson();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SocketClientHelper() {
        this("localhost", 6969);
    }

    public void close() {
        try {
            if(out != null) out.close();
            //if(in != null) in.close();
            if(socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(String request) {
        System.out.println("Sending request: " + request);
        out.println(request);
    }

    public void sendObject(Object obj) {
        String json = gson.toJson(obj);
        sendRequest(json);
    }

    public String receiveResponse(){
        try(ServerSocket responseSocket = new ServerSocket(9696)){
            Socket responseClientSocket = responseSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(responseClientSocket.getInputStream()));

            String response = in.readLine();

            in.close();
            responseClientSocket.close();

            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> T receiveObject(Type clazz) throws IOException {
        String json = receiveResponse();
        return gson.fromJson(json, clazz);
    }

    public List<LinkedHashMap<String, Object>> receiveLinkedHashMapList() throws IOException {
        Type type = new TypeToken<List<LinkedHashMap<String, Object>>>(){}.getType();
        String json = receiveResponse();
        return gson.fromJson(json, type);
    }
}
