package applications;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import controllers.user.HomepageController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
    private static final String SERVER_HOST = "localhost";
    private static final int PORT = 6969;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
        	// Create output and input streams for the socket
    		Socket socket = new Socket("localhost", Client.PORT);
    		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
    		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    		
//    		getDataFromServer();

    		launch(args);
    		// Get the file name from HomeController.fileName
    		if(HomepageController.fileName != null) {
    			String resultFile = HomepageController.fileName;

        		// Send the file name to the server
        		writer.println(resultFile);
        		System.out.println("Sent file name to server: " + resultFile);
    		} else if(HomepageController.folderName != null) {
    			// Get the file name from HomeController.fileName
        		String resultFolder = HomepageController.folderName;

        		// Send the file name to the server
        		writer.println(resultFolder);
        		System.out.println("Sent file name to server: " + resultFolder);
    		} else {
    			System.out.println("Khong co file hay folder nao duoc them");
    		}
    		

//    		// Optionally, read the response from the server
//    		String response = reader.readLine();
//    		System.out.println("Server response: " + response);
//
//    		String[] temp = response.split(",");
//    		List<String> tempString = Arrays.asList(temp);
//    		System.out.println(tempString);
    		
    		
    		
    		socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static List<String> getDataFromServer() {
	    try (Socket socket = new Socket("localhost", Client.PORT);
	         PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
	         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
	        String response = reader.readLine();
	        if (response != null) {
	            String[] temp = response.split(",");
	            return Arrays.asList(temp);
	        } else {
	            return new ArrayList<>();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return new ArrayList<>();
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
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader adminLoader = new FXMLLoader(MainApp.class.getResource("/view/client/dashboard.fxml"));
        Scene scene = new Scene(adminLoader.load(), 960, 540);
        stage.setTitle("Admin");
        stage.setScene(scene);
        stage.show();
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