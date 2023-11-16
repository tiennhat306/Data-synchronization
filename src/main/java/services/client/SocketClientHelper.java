package services.client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class SocketClientHelper {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ObjectInputStream in;

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

    public void sendFile(int size, String filePath) throws IOException {
        byte[] buffer = new byte[1024];
        try(FileInputStream fileInputStream = new FileInputStream(filePath)) {
            OutputStream fileOutputStream = socket.getOutputStream();
            int bytesRead;
            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            fileOutputStream.flush();
            System.out.println("File uploaded: " + filePath);
        }

    }

    public void sendFolder(int ownerId, String folderPath) throws IOException {
        File folder = new File(folderPath);

        int folderId = Integer.parseInt((String) receiveResponse());

        File[] listOfFiles = folder.listFiles();
        if(listOfFiles != null){
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    sendRequest("folder");
                    sendRequest(file.getName());
                    sendRequest(String.valueOf(ownerId));
                    sendRequest(String.valueOf(folderId));
                    sendFolder(ownerId, file.getAbsolutePath());
                } else { // if (file.isFile())
                    sendRequest("file");
                    sendRequest(file.getName());
                    sendRequest(String.valueOf(ownerId));
                    sendRequest(String.valueOf(folderId));
                    int size = (int) file.length();
                    sendRequest(String.valueOf(size));
                    sendFile(size ,file.getAbsolutePath());
                }
            }
            sendRequest("END_FOLDER");
        }
    }

    public void syncFile(String filePath, int size) {
        byte[] buffer = new byte[1024];

        try(FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            InputStream fileInputStream = socket.getInputStream();
            int bytesRead;
            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            System.out.println("File synchronized: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void syncFolder(String folderPath) {
        String type = (String) receiveResponse();
        while (!type.equals("END_FOLDER")) {
            if (type.equals("folder")) {
                String folderName = (String) receiveResponse();
                String newFolderPath = folderPath + java.io.File.separator + folderName;
                System.out.println("newFolderPath: " + newFolderPath);
                try {
                    Files.createDirectories(Paths.get(newFolderPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                syncFolder(newFolderPath);
            } else if (type.equals("file")) {
                String fileName = (String) receiveResponse();
                String size = (String) receiveResponse();
                syncFile(folderPath + java.io.File.separator + fileName, Integer.parseInt(size));
            }
            type = (String) receiveResponse();
        }
    }

    public boolean downloadFolder(String folderPath, int size) {
        String FolderZipPath = folderPath + ".zip";
        byte[] buffer = new byte[1024];

        try(FileOutputStream fileOutputStream = new FileOutputStream(FolderZipPath)) {
            InputStream fileInputStream = socket.getInputStream();
            int bytesRead;
            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            System.out.println("File downloaded: " + FolderZipPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
