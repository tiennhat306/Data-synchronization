package controllers.server;

import DTO.Connection;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.server.ServerCommunicationService;

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
    private TextField searchTxt;
    @FXML
    private HBox trashBtn;


    ServerCommunicationService server;

    public ConnectController() {
        server = new ServerCommunicationService();
        server.startListening();
    }
    public void populateConnectionData() {
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


}
