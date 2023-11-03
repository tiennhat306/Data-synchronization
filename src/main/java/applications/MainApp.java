package applications;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader adminLoader = new FXMLLoader(MainApp.class.getResource("/view/admin/dashboard.fxml"));
        Scene scene = new Scene(adminLoader.load(), 960, 540);
        stage.setTitle("Admin");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}