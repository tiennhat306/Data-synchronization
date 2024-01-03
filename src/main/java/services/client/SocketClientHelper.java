package services.client;

import applications.MainApp;
import enums.TypeEnum;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;


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
        this(MainApp.HOST, MainApp.PORT);
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

    public void sendFile(long size, String filePath) throws IOException {
        byte[] buffer = new byte[1024];
        try(FileInputStream fileInputStream = new FileInputStream(filePath)) {
            OutputStream fileOutputStream = socket.getOutputStream();
            long bytesRead;
            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, (int) bytesRead);
                size -= bytesRead;
            }
            fileOutputStream.flush();
        }

    }

    public void sendFolder(int ownerId, String folderPath) throws IOException {
        File folder = new File(folderPath);

        int folderId = Integer.parseInt((String) receiveResponse());
        if(folderId == -1) {
            return; // Error
        }

        File[] listOfFiles = folder.listFiles();
        if(listOfFiles != null){
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    sendRequest(String.valueOf(TypeEnum.FOLDER.getValue()));
                    sendRequest(file.getName());
                    sendRequest(String.valueOf(folderId));
                    sendFolder(ownerId, file.getAbsolutePath());
                } else { // if (file.isFile())
                    sendRequest(String.valueOf(TypeEnum.FILE.getValue()));
                    sendRequest(file.getName());
                    sendRequest(String.valueOf(folderId));
                    long size = (int) file.length();
                    sendRequest(String.valueOf(size));
                    sendFile(size ,file.getAbsolutePath());
                }
            }
            sendRequest(String.valueOf(TypeEnum.EOF.getValue()));
        }
    }

    public void syncFile(String filePath, long size) {
        byte[] buffer = new byte[1024];

        try(FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            InputStream fileInputStream = socket.getInputStream();
            long bytesRead;
            while(size > 0 && (bytesRead = fileInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, (int) bytesRead);
                size -= bytesRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void syncFolder(String folderPath) {
        int typeEnum = Integer.parseInt((String) receiveResponse());
        while (typeEnum != TypeEnum.EOF.getValue()) {
            if (typeEnum == TypeEnum.FOLDER.getValue()) {
                String folderName = (String) receiveResponse();
                String newFolderPath = folderPath + java.io.File.separator + folderName;
                try {
                    Files.createDirectories(Paths.get(newFolderPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                syncFolder(newFolderPath);
            } else if (typeEnum == TypeEnum.FILE.getValue()) {
                String fileName = (String) receiveResponse();
                long size = Integer.parseInt((String) receiveResponse());
                syncFile(folderPath + java.io.File.separator + fileName, size);
            }
            typeEnum = Integer.parseInt((String) receiveResponse());
        }
    }

    public void downloadFolder(String folderPath, long size) {
        String FolderZipPath = folderPath + ".zip";
        try{
            syncFile(FolderZipPath, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String filePath, long size) {
        try{
            syncFile(filePath, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
