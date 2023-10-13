package applications;

import models.File;
import models.Folder;
import models.Type;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import utils.HibernateUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Server {
    private static final String SERVER_HOST = "localhost";
    private static final String SERVER_PATH = "D:\\User\\Desktop\\Server";
    private static final int PORT = 6969;
    private static final int BUFFER_SIZE = 1024;
    private static SessionFactory sessionFactory;
    public static void main(String[] args) {
        // Khởi tạo Hibernate SessionFactory
        sessionFactory = HibernateUtil.getSessionFactory();

        try {
            ServerSocket serverSocket = new ServerSocket(Server.PORT);
            System.out.println("Server is waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from: " + clientSocket.getInetAddress());

                // Lắng nghe và đợi cho tên tệp và userPath từ máy khách
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String requestedFileName = reader.readLine();
                String userPath = reader.readLine();

                // Thực hiện truy vấn SQL thô để lấy thông tin từ MySQL sử dụng Hibernate
                Session session = sessionFactory.openSession();
                session.beginTransaction();

                String requestedName = requestedFileName.substring(0, requestedFileName.lastIndexOf("."));
                String requestedType = requestedFileName.substring(requestedFileName.lastIndexOf(".") + 1);
                System.out.println(requestedName);
                System.out.println(requestedType);

                String sqlQuery = "SELECT f FROM File f WHERE f.name = :requestedName and f.typesByTypeId.name = :requestedType";
                List<File> resultList = session.createQuery(sqlQuery, File.class)
                        .setParameter("requestedName", requestedName)
                        .setParameter("requestedType", requestedType)
                        .list();

                session.getTransaction().commit();
                session.close();

                // Gửi tệp cho máy khách nếu tìm thấy
                if (!resultList.isEmpty()) {
                    File file = resultList.get(0);
                    String fileName = file.getName() + "." + file.getTypesByTypeId().getName();
                    Folder folder = file.getFoldersByFolderId();
                    String folderPath = folder.getFolderName();
                    folder = folder.getFoldersByParentId();
                    while (folder != null) {
                        folderPath = folder.getFolderName() + java.io.File.separator + folderPath;
                        folder = folder.getFoldersByParentId();
                    }
                    sendFile(clientSocket, fileName, folderPath, userPath);
                } else {
                    // Báo cho máy khách rằng tệp không tồn tại
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println("File not found.");
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleFileRequest(Socket clientSocket) throws IOException {
        // Lắng nghe và đợi cho tên tệp và userPath từ máy khách
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String requestedFileName = reader.readLine();
        String userPath = reader.readLine();

        // Thực hiện truy vấn SQL thô để lấy thông tin từ MySQL sử dụng Hibernate
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        String requestedName = requestedFileName.substring(0, requestedFileName.lastIndexOf("."));
        String requestedType = requestedFileName.substring(requestedFileName.lastIndexOf(".") + 1);
        System.out.println(requestedName);
        System.out.println(requestedType);

        String sqlQuery = "SELECT f FROM File f WHERE f.name = :requestedName and f.typesByTypeId.name = :requestedType";
        List<File> resultList = session.createQuery(sqlQuery, File.class)
                .setParameter("requestedName", requestedName)
                .setParameter("requestedType", requestedType)
                .list();

        session.getTransaction().commit();
        session.close();

        // Gửi tệp cho máy khách nếu tìm thấy
        if (!resultList.isEmpty()) {
            File file = resultList.get(0);
            String fileName = file.getName() + "." + file.getTypesByTypeId().getName();
            Folder folder = file.getFoldersByFolderId();
            String folderPath = folder.getFolderName();
            folder = folder.getFoldersByParentId();
            while (folder != null) {
                folderPath = folder.getFolderName() + java.io.File.separator + folderPath;
                folder = folder.getFoldersByParentId();
            }
            sendFile(clientSocket, fileName, folderPath, userPath);
        } else {
            // Báo cho máy khách rằng tệp không tồn tại
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println("File not found.");
        }
    }

//    private static void handleSyncRequest(Socket socket) throws IOException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        String filePath = reader.readLine();
//        String userPath = reader.readLine();
//
//        // Sync the file to the server, including saving it to the file system, updating the database, and FTP transfer
//        syncFile(socket, filePath, userPath);
//    }

    private static void sendFile(Socket socket, String fileName, String folderPath, String userPath) throws IOException {
        // Đường dẫn đầy đủ cho tệp trên máy chủ
        String filePath = Server.SERVER_PATH + java.io.File.separator + folderPath + java.io.File.separator + fileName;

        // Gửi tên tệp trước
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(fileName);
        writer.println(userPath);

        // Gửi dữ liệu tệp
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        OutputStream fileOutputStream = socket.getOutputStream();
        fileOutputStream.write(data, 0, data.length);
        fileOutputStream.flush();

        // Truyền tệp qua FTP
        transferFileToFTP(filePath);
    }

//    private static void syncFile(Socket socket, String filePath, String userPath) throws IOException {
//        // Đường dẫn đầy đủ cho tệp trên máy chủ
//        String localFilePath = Server.SERVER_PATH + java.io.File.separator + filePath;
//
//        // Đọc dữ liệu tệp
//        InputStream fileInputStream = socket.getInputStream();
//        byte[] buffer = new byte[1024];
//        int bytesRead;
//        FileOutputStream fileOutputStream = new FileOutputStream(localFilePath);
//
//        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//            fileOutputStream.write(buffer, 0, bytesRead);
//        }
//
//        // Lưu tệp vào cơ sở dữ liệu
//        saveFileToDatabase(filePath, userPath);
//
//        // Đăng tệp lên máy chủ FTP
//        transferFileToFTP(localFilePath);
//    }

//    private static void saveFileToDatabase(String filePath, String userPath) {
//        // Thực hiện truy vấn SQL thô để lấy thông tin từ MySQL sử dụng Hibernate
//        Session session = sessionFactory.openSession();
//        session.beginTransaction();
//
//        String fileName = filePath.substring(filePath.lastIndexOf(java.io.File.separator) + 1);
//        String folderPath = filePath.substring(0, filePath.lastIndexOf(java.io.File.separator));
//        String folderName = folderPath.substring(folderPath.lastIndexOf(java.io.File.separator) + 1);
//
////        String sqlQuery = "SELECT f FROM Folder f WHERE f.folderName = :folderName";
////        List<Folder> resultList = session.createQuery(sqlQuery, Folder.class)
////                .setParameter("folderName", folderName)
////                .list();
//
//        File file = new File();
//        file.setName(fileName.substring(0, fileName.lastIndexOf(".")));
//
//        String typeName = fileName.substring(fileName.lastIndexOf(".") + 1);
//        String typeQuery = "SELECT t.id FROM Type t WHERE t.name = :typeName";
//        Integer typeId = session.createQuery(typeQuery, Type.class)
//                .setParameter("typeName", typeName)
//                .getFirstResult();
//
//        String folderQuery = "SELECT fd.id FROM Folder fd WHERE fd.folderName = :folderName";
//        Integer folderId = session.createQuery(folderQuery, Folder.class)
//                .setParameter("folderName", folderName)
//                .getFirstResult();
//
//        file.setTypeId(typeId);
//        file.setFolderId(folderId);
//        file.setOwnerId(4);
//        file.setSize(null);
//        session.save(file);
//        session.getTransaction().commit();
//
//
//        Folder folder = new Folder();
//        folder.setFolderName(folderName);
//
//
//        String parentFolderQuery = "SELECT fd.id FROM Folder fd WHERE fd.folderName = :parentName";
//        Integer parentId = session.createQuery(parentFolderQuery, Folder.class)
//                .setParameter("parentName", folderName)
//                .getFirstResult();
//        folder.setParentId();
//        folder.setOwnerId(4);
//        session.save(folder);
//
//        session.getTransaction().commit();
//        session.close();
//    }
//
    private static void transferFileToFTP(String filePath) {
        FTPClient ftpClient = new FTPClient();

        try {
            // Kết nối đến máy chủ FTP
            ftpClient.connect(Server.SERVER_HOST);
            ftpClient.login("root", "123456");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Lấy tên tệp từ đường dẫn
            java.io.File file = new java.io.File(filePath);
            String fileName = file.getName();

            // Lưu tệp lên máy chủ FTP
            FileInputStream inputStream = new FileInputStream(file);
            ftpClient.storeFile(fileName, inputStream);

            // Đăng xuất và đóng kết nối
            inputStream.close();
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
