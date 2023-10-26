package controllers.user;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import DTO.Item;
import models.Type;
import models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import services.client.user.ItemService;
import utils.HibernateUtil;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class HomepageController implements Initializable {

    @FXML
    private HBox HistoryBtn;
    @FXML
    private HBox SharedBtn;
    @FXML
    private HBox SharedByOtherBtn;
    @FXML
    private Label TrashBtn;
    @FXML
    private Button createFolderBtn;
    @FXML
    private TableView<Item> dataTable;
    @FXML
    private Label documentOwnerName;
    @FXML
    private HBox generalBtn;
    @FXML
    private ComboBox<String> lastUpdatedCbb;
    @FXML
    private ComboBox<User> ownerCbb;
    @FXML
    private HBox path;
    @FXML
    private HBox personalBtn;
    @FXML
    private FontAwesomeIconView searchBtn;
    @FXML
    private TextField searchTxt;
    @FXML
    private FontAwesomeIconView settingBtn;
    @FXML
    private ComboBox<Type> typeCbb;
    @FXML
    private Button uploadFileBtn;
    @FXML
    private Button uploadFolderBtn;
    @FXML
    private ImageView userAvatar;
    @FXML
    private Label userName;

    public HomepageController() {
    }

    public void populateData() {
        TableColumn<Item, String> nameColumn = new TableColumn<>("Tên");
        TableColumn<Item, String> ownerNameColumn = new TableColumn<>("Chủ sở hữu");
        TableColumn<Item, Date> dateModifiedColumn = new TableColumn<>("Đã sửa đổi");
        TableColumn<Item, String> lastModifiedByColumn = new TableColumn<>("Người sửa đổi");
        TableColumn<Item, String> sizeColumn = new TableColumn<>("Kích thước");

        dataTable.getColumns().addAll(nameColumn, ownerNameColumn, dateModifiedColumn, lastModifiedByColumn, sizeColumn);

//        final ObservableList<Item> data = FXCollections.observableArrayList(
//                new Item(1, 1, "test", "test", new Date(), "test", "test"),
//                new Item(2, 2, "test", "test", new Date(), "test", "test"),
//                new Item(3, 3, "test", "test", new Date(), "test", "test"),
//                new Item(4, 4, "test", "test", new Date(), "test", "test"),
//                new Item(5, 5, "test", "test", new Date(), "test", "test")
//        );

        nameColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("name"));
        ownerNameColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("ownerName"));
        dateModifiedColumn.setCellValueFactory(new PropertyValueFactory<Item, Date>("dateModified"));
        lastModifiedByColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("lastModifiedBy"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("size"));

        //dataTable.setItems(data);

        // log data
        //System.out.println("data: " + data);

        // log dataTable
        //System.out.println("dataTable: " + dataTable);


        ItemService itemService = new ItemService();
        List<Item> itemList = itemService.getAllItem(2);

        // log itemList
        System.out.println("itemList: " + itemList);

        if(itemList == null) {
            System.out.println("null");
            dataTable.setPlaceholder(new Label("Không có dữ liệu"));
        }
        else {
            final ObservableList<Item> items = FXCollections.observableArrayList(itemList);
            dataTable.setItems(items);
            System.out.println("not null");
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateData();
    }
}
