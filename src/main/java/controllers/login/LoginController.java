package controllers.login;

import DTO.LoginSession;
import applications.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import services.login.LoginService;
import services.server.admin.UserService;
import utils.HibernateUtil;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

public class LoginController {
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Label errorField;
    @FXML
    private PasswordField hiddenPasswordTextField;
    @FXML
    private CheckBox showPassword;
    private Session session;
    private Stage stage;
    private Scene scene;
    private Parent root;
    public LoginController() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        this.session = sessionFactory.openSession();
    }
    public LoginController(Session session) {
        this.session = session;
    }
    HashMap<String, String> loginInfo = new HashMap<>();
    Encryptor encryptor = new Encryptor();
    byte[] encryptionKey = {65, 12, 12, 12, 12, 12, 12, 12, 12,
            12, 12, 12, 12, 12, 12, 12};
    @FXML
    void changeVisibility(ActionEvent event) {
        if (showPassword.isSelected()) {
            passwordTextField.setText(hiddenPasswordTextField.getText());
            passwordTextField.setVisible(true);
            hiddenPasswordTextField.setVisible(false);
            return;
        }
        hiddenPasswordTextField.setText(passwordTextField.getText());
        hiddenPasswordTextField.setVisible(true);
        passwordTextField.setVisible(false);
    }
    @FXML
    void loginHandler(ActionEvent event) throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String username = usernameTextField.getText();
        String password = getPassword();

        if (username.isEmpty() && password.isEmpty()) {
            errorField.setText("Vui lòng nhập thông tin!");
        } else if (username.isEmpty()) {
            errorField.setText("Vui lòng nhập username!");
        } else if (password.isEmpty()) {
            errorField.setText("Vui lòng nhập password!");
        } else {
            updateUsernamesAndPasswords();
            String encryptedPassword = loginInfo.get(username);
            if (encryptedPassword != null) {
                if(password.equals(encryptor.decrypt(encryptedPassword,encryptionKey))){
                    UserService userService = new UserService();
                    User user = userService.getUserByUserName(username);
                    LoginSession loginSession = LoginService.getCurrentSession();
                    short numRole = user.getRole();
                    String role = null;
                    if (numRole == 1) role = "Client";
                    else if (numRole == 2) role = "Admin";
                    loginSession.createSession(user.getId(), user.getName(), user.getUsername(), role);
                    if (numRole == 1) {
                        root = FXMLLoader.load(MainApp.class.getResource("/view/user/dashboard.fxml"));
                        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                        scene = new Scene(root, 960, 540);
                        stage.setScene(scene);
                        stage.setTitle("Client");
                        stage.show();
                    } else if (numRole == 2) {
                        root = FXMLLoader.load(MainApp.class.getResource("/view/admin/dashboard.fxml"));
                        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                        scene = new Scene(root, 960, 540);
                        stage.setScene(scene);
                        stage.setTitle("Admin");
                        stage.show();
                    }
                } else {
                    errorField.setText("Sai username hoặc password!");
                }
            }
            else {
                errorField.setText("Sai username hoặc password!");
            }
        }
    }
    private String getPassword(){
        String passwordText = passwordTextField.getText();
        String hiddenPasswordText = hiddenPasswordTextField.getText();
        if (passwordText != "") return passwordText;
        return hiddenPasswordText;
    }
    private void updateUsernamesAndPasswords() throws IOException {
        loginInfo.clear();
        loginInfo = new HashMap<>();
        UserService userService = new UserService();
        List<User> userList = userService.getAllUser();
        if (userList != null) {
            for (User u : userList) {
                User user = userService.getUserById(u.getId());
                loginInfo.put(user.getUsername(), user.getPassword());
                System.out.println(user.getUsername() + " " + user.getPassword());
            }
        }
    }
}