package applications;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader userLoader = new FXMLLoader(MainApp.class.getResource("/view/admin/connection.fxml"));
//        Scene scene = new Scene(userLoader.load(), 960, 540);
////        scene.getStylesheets().add(Objects.requireNonNull(MainApp.class.getResource("/assets/css/style_hmm.css")).toExternalForm());
//        stage.setTitle("Client");
        FXMLLoader adminLoader = new FXMLLoader(MainApp.class.getResource("/view/user/dashboard.fxml"));
        Scene scene = new Scene(adminLoader.load(), 960, 540);
        stage.setTitle("Admin - Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}