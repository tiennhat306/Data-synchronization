package applications;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MainApp extends Application {
    public static String HOST;
    public static int PORT;
    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader userLoader = new FXMLLoader(MainApp.class.getResource("/view/admin/dashboard.fxml"));
//        Scene scene = new Scene(userLoader.load(), 960, 540);
//        scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource("/assets/css/style_hmm.css")).toExternalForm());
//        stage.setTitle("Client");
//        FXMLLoader adminLoader = new FXMLLoader(MainApp.class.getResource("/view/client/dashboard.fxml"));
        FXMLLoader loginLoader = new FXMLLoader(MainApp.class.getResource("/view/login/login-view.fxml"));
        Scene scene = new Scene(loginLoader.load());
        stage.setTitle("Login");
//        FXMLLoader loginLoader = new FXMLLoader(MainApp.class.getResource("/view/user/management.fxml"));
//        Scene scene = new Scene(loginLoader.load());
//        stage.setTitle("Login");
//        FXMLLoader adminLoader = new FXMLLoader(MainApp.class.getResource("/view/user/dashboard.fxml"));
//        Scene scene = new Scene(adminLoader.load(), 960, 540);
//        stage.setTitle("Admin - Client");
//        FXMLLoader adminLoader = new FXMLLoader(MainApp.class.getResource("/view/admin/dashboard.fxml"));
//        Scene scene = new Scene(adminLoader.load(), 960, 540);
//        stage.setTitle("Admin - Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        ResourceBundle application = ResourceBundle.getBundle("application");
        try {
            HOST = application.getString("server.host");
        } catch (MissingResourceException e) {
            System.out.println("Missing server.host in application.properties");
            HOST = "localhost";
        }

        if(HOST.isEmpty()) HOST = "localhost";

        try {
            PORT = Integer.parseInt(application.getString("server.port"));
        } catch (MissingResourceException e) {
            System.out.println("Missing server.port in application.properties");
            PORT = 6969;
        } catch (NumberFormatException e) {
            System.out.println("server.port in application.properties must be a number");
            PORT = 6969;
        } catch (Exception e) {
            PORT = 6969;
        }

        launch();
    }
}