package controllers.user;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import models.User;

public class HomepageController {

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
    private TableView<?> dataTable;

    @FXML
    private Label documentOwnerName;

    @FXML
    private HBox generalBtn;

    @FXML
    private ComboBox<User> lastUpdatedCbb;

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
    private ComboBox<?> typeCbb;

    @FXML
    private Button uploadFileBtn;

    @FXML
    private Button uploadFolderBtn;

    @FXML
    private ImageView userAvatar;

    @FXML
    private Label userName;

}
