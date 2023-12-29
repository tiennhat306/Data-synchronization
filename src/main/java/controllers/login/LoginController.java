package controllers.login;

import DTO.UserSession;
import applications.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import services.client.auth.LoginService;
import utils.HibernateUtil;
import java.io.IOException;


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
    void loginHandler(ActionEvent event) throws IOException{
        String username = usernameTextField.getText();
        String password = getPassword();

        if (username.isEmpty() && password.isEmpty()) {
            errorField.setText("Vui lòng nhập thông tin!");
        } else if (username.isEmpty()) {
            errorField.setText("Vui lòng nhập username!");
        } else if (password.isEmpty()) {
            errorField.setText("Vui lòng nhập password!");
        } else {
            UserSession userDTO = new LoginService().login(username, password);
            System.out.println("userDTO: " + userDTO.getRoleId());
            if(userDTO == null) {
                errorField.setText("Sai username hoặc password!");
            } else {
                errorField.setText("");
                UserSession userSession = LoginService.getCurrentSession();
                userSession.createSession(userDTO.getUserId(), userDTO.getName(), userDTO.getRoleId(), userDTO.getAvatar());
                short numRole = userSession.getRoleId();
                if(numRole == 1) {
                        root = FXMLLoader.load(MainApp.class.getResource("/view/user/dashboard.fxml"));
                        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                        scene = new Scene(root, 960, 540);
                        stage.setScene(scene);
                        stage.setTitle("Client");
                        stage.show();
                } else if(numRole == 2) {
                        root = FXMLLoader.load(MainApp.class.getResource("/view/admin/dashboard.fxml"));
                        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                        scene = new Scene(root, 960, 540);
                        stage.setScene(scene);
                        stage.setTitle("Admin");
                        stage.show();
                } else {
                    errorField.setText("Sai username hoặc password!");
                }
            }
        }
    }
    private String getPassword(){
        String passwordText = passwordTextField.getText();
        String hiddenPasswordText = hiddenPasswordTextField.getText();
        if (passwordText != "") return passwordText;
        return hiddenPasswordText;
    }
}