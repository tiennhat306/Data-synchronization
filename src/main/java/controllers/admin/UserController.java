package controllers.admin;

import DTO.Item;
import DTO.UserData;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import services.admin.UserService;
import utils.HibernateUtil;

import java.net.URL;
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
    private TableView<UserData> userTable;
    @FXML
    private HBox usersBtn;

    private Session session;
    public UserController() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        this.session = sessionFactory.openSession();
    }
    public UserController(Session session) {
        this.session = session;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateData();
    }

    public void populateData() {
        TableColumn<UserData, String> nameColumn = new TableColumn<>("Tên");
        TableColumn<UserData, String> genderColumn = new TableColumn<>("Giới tính");
        TableColumn<UserData, Date> birthdayColumn = new TableColumn<>("Ngày sinh");
        TableColumn<UserData, String> phoneNumberColumn = new TableColumn<>("Số điện thoại");
        TableColumn<UserData, String> emailColumn = new TableColumn<>("Email");
        TableColumn<UserData, String> roleColumn = new TableColumn<>("Chức vụ");

        userTable.getColumns().addAll(nameColumn, genderColumn, birthdayColumn, phoneNumberColumn, emailColumn, roleColumn);

        nameColumn.setCellValueFactory(new PropertyValueFactory<UserData, String>("name"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<UserData, String>("gender"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<UserData, Date>("birthday"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<UserData, String>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<UserData, String>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<UserData, String>("role"));

        UserService userService = new UserService(session);
        List<UserData> userList = userService.getAllUser();

        System.out.println("userList: " + userList);

        if(userList == null) {
            userTable.setPlaceholder(new Label("Không có dữ liệu người dùng"));
        }
        else {
            final ObservableList<UserData> users = FXCollections.observableArrayList(userList);
            userTable.setItems(users);
        }

    }
}
