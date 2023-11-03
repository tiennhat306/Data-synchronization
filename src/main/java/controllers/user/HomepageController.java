package controllers.user;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import models.File;
import models.Type;
import models.User;
import services.client.user.ItemService;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private TableView<File> dataTable;
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
        TableColumn<File, String> nameColumn = new TableColumn<>("Tên");
        TableColumn<File, String> ownerNameColumn = new TableColumn<>("Chủ sở hữu");
        TableColumn<File, Date> dateModifiedColumn = new TableColumn<>("Đã sửa đổi");
        TableColumn<File, String> lastModifiedByColumn = new TableColumn<>("Người sửa đổi");
        TableColumn<File, String> sizeColumn = new TableColumn<>("Kích thước");

        dataTable.getColumns().addAll(nameColumn, ownerNameColumn, dateModifiedColumn, lastModifiedByColumn, sizeColumn);

        nameColumn.setCellValueFactory(column -> {
            return new SimpleStringProperty(column.getValue().getName() + (column.getValue().getTypeId() != 1 ? "." + column.getValue().getTypesByTypeId().getName() : ""));
        });
        ownerNameColumn.setCellValueFactory(column -> {
            return new SimpleStringProperty(column.getValue().getUsersByOwnerId().getName());
        });
        dateModifiedColumn.setCellFactory(column -> {
            return new TableCell<File, Date>() {
                private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
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
        dateModifiedColumn.setCellValueFactory(new PropertyValueFactory<File, Date>("updatedAt"));
        lastModifiedByColumn.setCellValueFactory(column -> {
            return new SimpleStringProperty(column.getValue().getUsersByUpdatedBy() == null ? "" : column.getValue().getUsersByUpdatedBy().getName());
        });
        sizeColumn.setCellValueFactory(column -> {
            int size = column.getValue().getSize();
            String sizeStr = "";
            if(size < 0){
                sizeStr = (size - Short.MIN_VALUE) + " mục";
            }
            else if(size < 1024) {
                sizeStr = size + " bytes";
            }
            else if(size < 1024 * 1024) {
                sizeStr = size / 1024 + " KB";
            }
            else if(size < 1024 * 1024 * 1024) {
                sizeStr = size / (1024 * 1024) + " MB";
            }
            else {
                sizeStr = size / (1024 * 1024 * 1024) + " GB";
            }
            return new SimpleStringProperty(sizeStr);
        });



        ItemService itemService = new ItemService();
        List<File> itemList = itemService.getAllItem(2);

        System.out.println("itemList: " + itemList);

        if(itemList == null) {
            System.out.println("null");
            dataTable.setPlaceholder(new Label("Không có dữ liệu"));
        }
        else {
            final ObservableList<File> items = FXCollections.observableArrayList(itemList);
            dataTable.setItems(items);
            System.out.println("not null");
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateData();
    }
}
