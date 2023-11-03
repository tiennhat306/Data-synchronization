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

public class ServerApp extends Application {
    public static ObservableList<Connection> connections = FXCollections.observableArrayList();
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
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
