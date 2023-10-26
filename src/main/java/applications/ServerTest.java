package applications;

import models.File;
import models.Folder;
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

public class ServerTest {
    private static final String SERVER_HOST = "localhost";
    private static final String SERVER_PATH = "D:\\User\\Desktop\\Server";
    private static final int PORT = 9696;
    private static final int BUFFER_SIZE = 1024;
    private static SessionFactory sessionFactory;
    public static void main(String[] args) {
        // Khởi tạo Hibernate SessionFactory
        sessionFactory = HibernateUtil.getSessionFactory();

        try {
            ServerSocket serverSocket = new ServerSocket(ServerTest.PORT);
            System.out.println("ServerTest is waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("ClientTest connected from: " + clientSocket.getInetAddress());

                // Lắng nghe và đợi cho tên tệp và userPath từ máy khách
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                String requestedFileName = reader.readLine();
                String userPath = reader.readLine();

                writer.println("OK server da nhan duoc.");

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
                    while (folder.getFoldersByParentId() != null) {
                        folderPath = folder.getFolderName() + java.io.File.separator + folderPath;
                        folder = folder.getFoldersByParentId();
                    }
                    sendFile(clientSocket, writer, fileName, folderPath, userPath);

                    writer.println("Server: Da gui file");
                } else {
                    // Báo cho máy khách rằng tệp không tồn tại
                    writer.println("File not found.");
                }


//                String response = reader.readLine();
//                if(response.equals("Client done")){
//                    System.out.println("Client done");
//                    clientSocket.close();
//                } else {
//                    System.out.println("Client not done");
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("HeheheheheheehServer");
    }



    private static void     sendFile(Socket socket, PrintWriter writer, String fileName, String folderPath, String userPath) throws IOException {
        // Đường dẫn đầy đủ cho tệp trên máy chủ
        String filePath = ServerTest.SERVER_PATH + java.io.File.separator + folderPath + java.io.File.separator + fileName;

        // Gửi tên tệp trước
        //PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        writer.println(fileName);
        writer.println(userPath);

        // Gửi dữ liệu tệp
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        OutputStream fileOutputStream = socket.getOutputStream();
        fileOutputStream.write(data, 0, data.length);
        fileOutputStream.flush();

        writer.println("Server: Da gui file");

        // Truyền tệp qua FTP
        //transferFileToFTP(filePath);


    }

//    private static void transferFileToFTP(String filePath) {
//        FTPClient ftpClient = new FTPClient();
//
//        try {
//            // Kết nối đến máy chủ FTP
//            ftpClient.connect(ServerTest.SERVER_HOST);
//            ftpClient.login("root", "123456");
//            ftpClient.enterLocalPassiveMode();
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//
//            // Lấy tên tệp từ đường dẫn
//            java.io.File file = new java.io.File(filePath);
//            String fileName = file.getName();
//
//            // Lưu tệp lên máy chủ FTP
//            FileInputStream inputStream = new FileInputStream(file);
//            ftpClient.storeFile(fileName, inputStream);
//
//            // Đăng xuất và đóng kết nối
//            inputStream.close();
//            ftpClient.logout();
//            ftpClient.disconnect();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
