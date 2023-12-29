package controllers.server;

import DTO.Connection;
import applications.ServerApp;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import services.server.ServerCommunicationService;

import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import static applications.ServerApp.connections;

public class ConnectController implements Initializable {
    @FXML
    private HBox connectionBtn;
    @FXML
    private TableView<Connection> connectionTable;
    @FXML
    private HBox dataBtn;
    @FXML
    private Label documentOwnerName;
    @FXML
    private TableView<Connection> historyTable;
    @FXML
    private FontAwesomeIconView searchBtn;
    @FXML
    private TextField addressField;
    @FXML
    private TextField portField;
    @FXML
    private Button changePortBtn;
    @FXML
    private HBox trashBtn;

    private int port;
    ServerCommunicationService server;

    public ConnectController() {
        port = ServerApp.PORT;
        server = new ServerCommunicationService(port);
        server.startListening();
    }
    public void populateConnectionData() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            addressField.setText(address.getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        portField.setText(String.valueOf(ServerApp.PORT));

        TableColumn<Connection, String> addressColumn = new TableColumn<>("Máy kết nối");
        TableColumn<Connection, String> requestColumn = new TableColumn<>("Yêu cầu");
        TableColumn<Connection, Date> requestTimeColumn = new TableColumn<Connection, Date>("Thời gian yêu cầu");

        connectionTable.getColumns().addAll(addressColumn, requestColumn, requestTimeColumn);

        addressColumn.setCellValueFactory(new PropertyValueFactory<Connection, String>("address"));
        requestColumn.setCellValueFactory(new PropertyValueFactory<Connection, String>("request"));
        requestTimeColumn.setCellFactory(column -> {
            return new TableCell<Connection, Date>() {
                private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty) {
                        setText(null);
                    }
                    else {
                        setText(format.format(item));
                    }
                }
            };
        });
        requestTimeColumn.setCellValueFactory(new PropertyValueFactory<Connection, Date>("requestTime"));

        connectionTable.setItems(connections);



    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateConnectionData();
    }


    @FXML
    public void changePort(MouseEvent mouseEvent) {
        try {
            int newPort = Integer.parseInt(portField.getText());
            ServerApp.PORT = newPort;
            this.port = newPort;
            server.stopListening();
            server = new ServerCommunicationService(port);
            server.startListening();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Cổng không hợp lệ");
            alert.setContentText("Cổng phải là một số nguyên dương");
            alert.showAndWait();
        }
    }
}
