package services.client;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientCommunicationService {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public void connectToServer(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendRequestAndGetResponse(String request) {
        try {
            output.writeObject(request);
            output.flush();
            return (String) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Error";
        }
    }
}
