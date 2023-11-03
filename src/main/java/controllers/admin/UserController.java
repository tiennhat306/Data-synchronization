package controllers.admin;

import DTO.UserData;
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
import models.User;
import services.client.admin.UserService;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    @FXML
    private HBox HistoryBtn;
    @FXML
    private Button addUserBtn;
    @FXML
    private HBox dataBtn;
    @FXML
    private Label documentOwnerName;
    @FXML
    private Label historyBtn;
    @FXML
    private Pagination pagination;
    @FXML
    private HBox path;
    @FXML
    private ComboBox<String> roleCbb;
    @FXML
    private FontAwesomeIconView searchBtn;
    @FXML
    private TextField searchTxt;
    @FXML
    private FontAwesomeIconView userSearchBtn;
    @FXML
    private TextField userSearchTxt;
    @FXML
    private FontAwesomeIconView settingBtn;
    @FXML
    private ImageView userAvatar;
    @FXML
    private Label userName;
    @FXML
    private TableView<User> userTable;
    @FXML
    private HBox usersBtn;

    public UserController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateData();
    }

    public void populateData() {
        TableColumn<User, String> nameColumn = new TableColumn<>("Tên");
        TableColumn<User, String> genderColumn = new TableColumn<>("Giới tính");
        TableColumn<User, Date> birthdayColumn = new TableColumn<>("Ngày sinh");
        TableColumn<User, String> phoneNumberColumn = new TableColumn<>("Số điện thoại");
        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        TableColumn<User, String> roleColumn = new TableColumn<>("Chức vụ");

        userTable.getColumns().addAll(nameColumn, genderColumn, birthdayColumn, phoneNumberColumn, emailColumn, roleColumn);

        nameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("name"));
        //genderColumn.setCellValueFactory(new PropertyValueFactory<User, String>("gender"));
        genderColumn.setCellValueFactory(column -> {
            return column.getValue().getGender() ? new SimpleStringProperty("Nam") : new SimpleStringProperty("Nữ");
        });
        birthdayColumn.setCellFactory(column -> {
            return new TableCell<User, Date>() {
                private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
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
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<User, Date>("birthday"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<User, String>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<User, String>("email"));
        //roleColumn.setCellValueFactory(new PropertyValueFactory<User, String>("role"));
        roleColumn.setCellValueFactory(column -> {
            return column.getValue().getRole() == 1 ? new SimpleStringProperty("Quản trị viên") : new SimpleStringProperty("Người dùng");
        });

        UserService userService = new UserService();
        List<User> userList = userService.getAllUser();

        System.out.println("userList: " + userList);

        if(userList == null) {
            userTable.setPlaceholder(new Label("Không có dữ liệu người dùng"));
        }
        else {
            ObservableList<User> users = FXCollections.observableArrayList(userList);
            userTable.setItems(users);
        }

    }
}
