package services.client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class SocketClientHelper {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public SocketClientHelper(String host, int port) {
        try{
            socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, 20000);
            socket.setSoTimeout(10000);
            //socket.setSoLinger(true, 5000);
            System.out.println("Connected to server: " + socket.getInetAddress());
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
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
            if(in != null) in.close();
            if(socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(Object request) throws IOException {
        System.out.println("Sending request: " + request);
        out.writeObject(request);
        out.flush();
    }


    public Object receiveResponse(){
        try{
            Object response = in.readObject();

            return response;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendFile(String fileName, int ownerId, int folderId, int size, String filePath) throws IOException {
//        File file = new File(filePath);
//        byte[] buffer = new byte[1024];
//
//        try(FileInputStream fileInputStream = new FileInputStream(file);
//            OutputStream fileOutputStream = socket.getOutputStream()) {
//            int bytesRead;
//            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//                fileOutputStream.write(buffer, 0, bytesRead);
//                fileOutputStream.flush();
//            }
//
//            System.out.println("File uploaded: " + fileName);
//        }

        // send file
        byte[] buffer = new byte[1024];
        try(FileInputStream fileInputStream = new FileInputStream(filePath);
            OutputStream fileOutputStream = socket.getOutputStream()) {
            int bytesRead;
            //while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            System.out.println("File uploaded: " + filePath);
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
}
