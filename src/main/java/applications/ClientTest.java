package applications;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.net.Socket;

public class ClientTest {
    private static final String SERVER_HOST = "localhost";
    private static final int PORT = 9696;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(ClientTest.SERVER_HOST, ClientTest.PORT);

            // Lựa chọn chức năng (1: Yêu cầu file, 2: Đồng bộ file)
//            System.out.print("Select an option (1: Request file, 2: Sync file): ");
//            int requestType = Integer.parseInt(inputReader.readLine());
//            System.out.print("Enter the file name: ");
//            String requestedFileName = inputReader.readLine();
//            System.out.print("Enter the user path: ");
//            String userPath = inputReader.readLine();
            if(socket.isConnected()){
                System.out.println("Connected to server.");
            } else {
                System.out.println("Failed to connect to server.");
            }
            String requestedFileName = "cheems.jpg";
            String userPath = "D:\\User\\Desktop\\Client";
            // if requestedFileName exists in userPath, then delete it
            File file = new File(userPath + File.separator + requestedFileName);
            if(file.exists()){
                boolean delete = file.delete();
                if(delete){
                    System.out.println("Deleted file: " + file.getAbsolutePath());
                } else {
                    System.out.println("Failed to delete file: " + file.getAbsolutePath());
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Gửi tên tệp và userPath đến máy chủ
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            writer.println(requestedFileName);
            writer.println(userPath);

            String response1 = reader.readLine();
            System.out.println(response1);


            // Đọc đường dẫn tệp và userPath từ máy chủ
            String response = reader.readLine();
            String response3 = "";

            if (response.equals("File not found.")) {
                System.out.println("File not found on the server.");
            } else {
                String filePath = response;
                String receivedUserPath = reader.readLine();
                receiveAndStoreFile(socket, reader, filePath, receivedUserPath);
                response3 = reader.readLine();
                System.out.println("response 3: "+response3);
            }

            if(response3.equals("Server: Da gui file")){
                System.out.println("OK server da nhan duoc.");
                socket.close();
            } else {
                System.out.println("ServerTest error.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null) socket.close();
        }
        System.out.println("HeheheheheheehClient");
    }

    private static void receiveAndStoreFile(Socket socket, BufferedReader reader, String filePath, String userPath) throws IOException {
        // Đường dẫn đầy đủ cho tệp trên máy khách
        String localFilePath = userPath + File.separator + new File(filePath).getName();

        // Đọc dữ liệu tệp
        InputStream fileInputStream = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        FileOutputStream fileOutputStream = new FileOutputStream(localFilePath);

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
        }

        fileOutputStream.close();
        System.out.println("File received and stored: " + localFilePath);;

        // Đọc response từ server
        String response2 = reader.readLine();
        System.out.println("response 2: "+response2);

        // Sau khi tệp đã được lưu trữ, bạn có thể thực hiện tải nó từ máy chủ FTP
        //downloadFileFromFTP(filePath);
    }

//    private static void downloadFileFromFTP(String filePath) {
//        FTPClient ftpClient = new FTPClient();
//
//        try {
//            // Kết nối đến máy chủ FTP
//            ftpClient.connect(ClientTest.SERVER_HOST);
//            ftpClient.login("root", "123456");
//            ftpClient.enterLocalPassiveMode();
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//
//            // Lấy tên tệp từ đường dẫn
//            File file = new File(filePath);
//            String fileName = file.getName();
//
//            // Đường dẫn đầy đủ cho tệp trên máy chủ FTP
//            String ftpFilePath = "/" + fileName;
//
//            // Đường dẫn đầy đủ cho tệp trên máy khách
//            String localFilePath = new File(filePath).getName();
//
//            // Tải tệp từ máy chủ FTP và lưu trữ trên máy khách
//            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFilePath));
//            ftpClient.retrieveFile(ftpFilePath, outputStream);
//
//            // Đăng xuất và đóng kết nối
//            outputStream.close();
//            ftpClient.logout();
//            ftpClient.disconnect();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}