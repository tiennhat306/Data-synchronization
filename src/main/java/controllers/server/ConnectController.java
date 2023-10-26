package controllers.server;

import DTO.Connection;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.server.ServerCommunicationService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
        server = new ServerCommunicationService(9696);
        server.startListening();
    }
    public void populateConnectionData() {
        TableColumn<Connection, String> addressColumn = new TableColumn<>("Máy kết nối");
        TableColumn<Connection, Integer> portColumn = new TableColumn<>("Cổng");

        connectionTable.getColumns().addAll(addressColumn, portColumn);

        addressColumn.setCellValueFactory(new PropertyValueFactory<Connection, String>("address"));
        portColumn.setCellValueFactory(new PropertyValueFactory<Connection, Integer>("port"));

        List<Connection> connections = null;
        connections = server.getAllConnection();
        System.out.println("connectionList: " + connections);

        if(connections == null || connections.size() == 0) {
            connectionTable.setPlaceholder(new Label("Không có kết nối"));
        }
        else {
            final ObservableList<Connection> items = FXCollections.observableArrayList(connections);
            connectionTable.setItems(items);
        }

    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateConnectionData();
    }


}
