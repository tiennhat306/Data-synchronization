package services.client;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;


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

    public void sendFile(String fileName, int ownerId, int folderId, int size, String filePath) throws IOException {
        File file = new File(filePath);
        byte[] buffer = new byte[1024];

        try(FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream fileOutputStream = socket.getOutputStream()) {
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.flush();

            System.out.println("File uploaded: " + fileName);
        }
    }

    public void sendFolder(String folderName, int ownerId, int parentId , String folderPath) throws IOException {
        File folder = new File(folderPath);

        int folderId = Integer.parseInt((String) receiveResponse());

        File[] listOfFiles = folder.listFiles();
        boolean response = (folderId != -1);
        if(listOfFiles != null){
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    sendRequest("folder");
                    sendRequest(file.getName());
                    sendRequest(String.valueOf(ownerId));
                    sendRequest(String.valueOf(folderId));
                    sendFolder(file.getName(), ownerId, folderId ,file.getAbsolutePath());
                } else { // if (file.isFile())
                    sendRequest("file");
                    sendRequest(file.getName());
                    sendRequest(String.valueOf(ownerId));
                    sendRequest(String.valueOf(folderId));
                    int size = (int) file.length();
                    sendRequest(String.valueOf(size));
                    sendFile(file.getName(), ownerId, folderId, size ,file.getAbsolutePath());
                }
            }
            sendRequest("END_FOLDER");
        }
    }

    public void syncFile(String filePath) {

        try(ServerSocket responseSocket = new ServerSocket(9696)){
            Socket responseClientSocket = responseSocket.accept();

            try(InputStream fileInputStream = responseClientSocket.getInputStream()) {
                Files.copy(fileInputStream, Paths.get(filePath));
            }

            if(responseClientSocket != null) responseClientSocket.close();

            //return response;
        } catch (IOException e) {
            e.printStackTrace();
            //return null;
        }
    }

    public void syncFolder(String userPath) {

    }
}
