package applications;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int PORT = 6969;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(Client.SERVER_HOST, Client.PORT);

            // Lựa chọn chức năng (1: Yêu cầu file, 2: Đồng bộ file)
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
//            System.out.print("Select an option (1: Request file, 2: Sync file): ");
//            int requestType = Integer.parseInt(inputReader.readLine());
            System.out.print("Enter the file name: ");
            String requestedFileName = inputReader.readLine();
            System.out.print("Enter the user path: ");
            String userPath = inputReader.readLine();

            // Gửi tên tệp và userPath đến máy chủ
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(requestedFileName);
            writer.println(userPath);

            // Đọc đường dẫn tệp và userPath từ máy chủ
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();

            if (response.equals("File not found.")) {
                System.out.println("File not found on the server.");
            } else {
                String filePath = response;
                String receivedUserPath = reader.readLine();
                receiveAndStoreFile(socket, filePath, receivedUserPath);
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void receiveAndStoreFile(Socket socket, String filePath, String userPath) throws IOException {
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
        System.out.println("File received and stored: " + localFilePath);

        // Sau khi tệp đã được lưu trữ, bạn có thể thực hiện tải nó từ máy chủ FTP
        downloadFileFromFTP(filePath);
    }

    private static void downloadFileFromFTP(String filePath) {
        FTPClient ftpClient = new FTPClient();

        try {
            // Kết nối đến máy chủ FTP
            ftpClient.connect(Client.SERVER_HOST);
            ftpClient.login("root", "123456");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Lấy tên tệp từ đường dẫn
            File file = new File(filePath);
            String fileName = file.getName();

            // Đường dẫn đầy đủ cho tệp trên máy chủ FTP
            String ftpFilePath = "/" + fileName;

            // Đường dẫn đầy đủ cho tệp trên máy khách
            String localFilePath = new File(filePath).getName();

            // Tải tệp từ máy chủ FTP và lưu trữ trên máy khách
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFilePath));
            ftpClient.retrieveFile(ftpFilePath, outputStream);

            // Đăng xuất và đóng kết nối
            outputStream.close();
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}