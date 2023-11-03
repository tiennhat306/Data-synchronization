package applications;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.HibernateUtil;

import java.io.IOException;

public class ServerApp extends Application {

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