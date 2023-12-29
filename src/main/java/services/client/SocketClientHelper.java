package services.client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class SocketClientHelper {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public SocketClientHelper(String host, int port) {
        try{
            socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, 20000);
            socket.setSoTimeout(10000);
            socket.setSoLinger(true, 5000);
            System.out.println("Connected to server: " + socket.getInetAddress());
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SocketClientHelper() throws UnknownHostException {
        ResourceBundle application = ResourceBundle.getBundle("application");
        String host;
        int port;
        try {
            host = application.getString("server.host");
        } catch (MissingResourceException e) {
            System.out.println("Missing server.host in application.properties");
            host = "localhost";
        }

        if(host.isEmpty()) host = "localhost";

        try {
            port = Integer.parseInt(application.getString("server.port"));
        } catch (MissingResourceException e) {
            System.out.println("Missing server.port in application.properties");
            port = 6969;
        } catch (NumberFormatException e) {
            System.out.println("server.port in application.properties must be a number");
            port = 6969;
        } catch (Exception e) {
            port = 6969;
        }

        try{
            socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address, 20000);
            socket.setSoTimeout(10000);
            socket.setSoLinger(true, 5000);
            System.out.println("Connected to server: " + socket.getInetAddress());
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    
    public void renameFolder(String folderPath, String folderName) {
        try {            
            // Perform the renaming operation
            File folder = new File(folderPath);
            File newFolder = new File(folder.getParent(), folderName);
            boolean renameSuccess = folder.renameTo(newFolder);

            if (renameSuccess) {
                System.out.println("Rename folder " + folderName + " thành công");
            } else {
                System.out.println("Rename folder " + folderName + " thất bại");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void deleteFile(String filePath) {
        File fileToDelete = new File(filePath);
        if (fileToDelete.delete()) {
            System.out.println("File deleted successfully.");
        } else {
            System.out.println("Failed to delete the file.");
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

    public void downloadFolder(String folderPath, int size) {
        String FolderZipPath = folderPath + ".zip";
        try{
            syncFile(FolderZipPath, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        byte[] buffer = new byte[1024];
//
//        try(FileOutputStream fileOutputStream = new FileOutputStream(FolderZipPath)) {
//            InputStream fileInputStream = socket.getInputStream();
//            int bytesRead;
//            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, Math.min(buffer.length, size))) != -1) {
//                fileOutputStream.write(buffer, 0, bytesRead);
//                size -= bytesRead;
//            }
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    public void downloadFile(String filePath, int size) {
        try{
            syncFile(filePath, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
