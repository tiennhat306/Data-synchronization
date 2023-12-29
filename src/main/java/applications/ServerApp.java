package applications;

import DTO.Connection;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.HibernateUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ServerApp extends Application {
    public static int PORT;
    public static String SERVER_PATH;
    public static ObservableList<Connection> connections = FXCollections.observableArrayList();
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        ResourceBundle application = ResourceBundle.getBundle("application");
        try {
            PORT = Integer.parseInt(application.getString("server.port"));
        } catch (Exception e) {
            PORT = 6969;
        }
        try {
            SERVER_PATH = application.getString("server.path");
        } catch (MissingResourceException e) {
            System.out.println("Missing server.path in application.properties");
            SERVER_PATH = "D:\\User\\Desktop\\Server";
        } catch (Exception e) {
            SERVER_PATH = "D:\\User\\Desktop\\Server";
        }
        System.out.println("Server path: " + SERVER_PATH);
        System.out.println("Server port: " + PORT);
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApp.class.getResource("/view/server/connection.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 960, 540);
        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        HibernateUtil.close();
        super.stop();
        System.exit(0);
    }
}
