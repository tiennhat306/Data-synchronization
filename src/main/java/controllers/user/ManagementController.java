package controllers.user;

import DTO.UserAccountDTO;
import DTO.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.client.AccountService;
import services.client.admin.UserService;
import services.client.auth.LoginService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class ManagementController implements Initializable {
    @FXML
    private TabPane managementView;

    // Tab "Thông tin cá nhân"
    @FXML
    private Tab tabInfo;
    @FXML
    private TextField tfUsername;
    @FXML
    private TextField tfName;
    @FXML
    private TextField tfEmail;
    @FXML
    private TextField tfPhone;
    @FXML
    private DatePicker birthday;
    @FXML
    private RadioButton rdMale;
    @FXML
    private RadioButton rdFemale;
    @FXML
    private Label emailErr;
    @FXML
    private Label usernameErr;
    @FXML
    private Label phoneErr;
    @FXML
    private Button btnUpd;

    // Tab "Đổi mật khẩu"
    @FXML
    private Tab tabPass;
    @FXML
    private PasswordField tfOldPass;
    @FXML
    private PasswordField tfNewPass;
    @FXML
    private PasswordField tfConfirmPass;
    @FXML
    private Button btnSavePass;
    @FXML
    private Label oldPassErr;
    @FXML
    private Label newPassErr;
    @FXML
    private Label confirmPassErr;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UserSession userSession = LoginService.getCurrentSession();
        int userId = userSession.getUserId();
        UserAccountDTO userAccountInfo = new AccountService().getUserAccountInfo(userId);

        tfUsername.setText(userAccountInfo.getUsername());
        tfUsername.setEditable(false);
        UserService userService = new UserService();
        tfName.setText(userAccountInfo.getName());
        tfEmail.setText(userAccountInfo.getEmail());
        tfPhone.setText(userAccountInfo.getPhoneNumber());

        Date birthDate = userAccountInfo.getBirthday();
        // Chuyển đổi Date thành LocalDate
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthDate);
        LocalDate birthLocalDate = LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        // Gán giá trị LocalDate vào DatePicker
        birthday.setValue(birthLocalDate);
        if (userAccountInfo.isGender()) {
           rdFemale.setSelected(true);
        } else {
            rdMale.setSelected(true);
        }
    }
    // Phương thức được gọi khi nhấp vào nút "Cập nhật" trong Tab "Thông tin cá nhân"
    @FXML
    private void submitFormUpdate(ActionEvent event) throws IOException {
        // Xử lý logic cập nhật thông tin cá nhân
        boolean usernameError = textFieldIsNull(tfUsername, usernameErr, "Tên đăng nhập không được để trống!");
        boolean emailError = textFieldIsNull(tfEmail, emailErr, "Email không được để trống!");
        boolean phoneError = textFieldIsNull(tfPhone, phoneErr, "Số điện thoại không được để trống!");
        System.out.println(usernameError + " " + emailError + " " + phoneError);
        if (!usernameError) {
            usernameError = usernameFormat(tfName, usernameErr, "Tên đăng nhập sai định dạng");
        }
        if (!emailError) {
            emailError = emailFormat(tfEmail, emailErr, "Email sai định dạng");
        }
        if (!phoneError) {
            phoneError = phoneFormat(tfPhone, phoneErr, "Số điện thoại sai định dạng");
        }
        System.out.println(usernameError + " " + emailError + " " + phoneError);
        if ((!usernameError) && (!emailError) && (!phoneError)) {
            String usernameForm = tfUsername.getText();
            String nameForm = tfName.getText();
            String mailForm = tfEmail.getText();
            String phoneForm = tfPhone.getText();
            LocalDate selectedDate = birthday.getValue();
            java.sql.Date date = java.sql.Date.valueOf(selectedDate);
            boolean genderForm = (rdFemale.isSelected()) ? true : false;
            UserService userService = new UserService();
            UserSession userSession = LoginService.getCurrentSession();
            int userId = userSession.getUserId();
            boolean success = new AccountService().updateUserInfo(userId, usernameForm, nameForm, mailForm, phoneForm, date, genderForm);
            if (success) {
                // Đóng form hiện tại
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user/dashboard.fxml"));
                Parent root = loader.load();
                HomepageController oldFormController = loader.getController();
                oldFormController.refreshName(nameForm);
                Stage currentStage = (Stage) tfUsername.getScene().getWindow();
                currentStage.close();
            }
        }
    }

    // Phương thức được gọi khi nhấp vào nút "Lưu" trong Tab "Đổi mật khẩu"
    @FXML
    private void submitFormPass(ActionEvent event)  {
        boolean oldPassError = textFieldIsNull(tfOldPass, oldPassErr, "Mật khẩu cũ không được để trống!");
        boolean newPassError = textFieldIsNull(tfNewPass, newPassErr, "Mật khẩu mới không được để trống!");
        boolean confirmPassError = textFieldIsNull(tfConfirmPass, confirmPassErr, "Xác nhận mật khẩu không được để trống!");
        if (!newPassError) {
//            newPassError = passFormat(tfNewPass, newPassErr, "Mật khẩu chưa đủ mạnh");
        }
        if (!confirmPassError) {
            confirmPassError = confirmPassMatch(tfNewPass, tfConfirmPass, confirmPassErr, "Xác nhận mật khẩu không khớp!");
        }
        if ((!oldPassError) && (!newPassError) && (!confirmPassError)) {
            UserSession userSession = LoginService.getCurrentSession();
            int userId = userSession.getUserId();
            boolean success = new AccountService().updatePassword(userId, tfOldPass.getText(), tfNewPass.getText());
            Stage currentStage = (Stage) tfOldPass.getScene().getWindow();
            currentStage.close();
        }
    }

    public static boolean usernameFormat(TextField inputTextField, Label inputLabel, String validationText) {
        boolean noValid = false;
        String validationString = null;

//        if (!inputTextField.getText().matches("[a-zA-Z0-9_]+")) {
//            noValid = true;
//            validationString = validationText;
//        }
//
//        inputLabel.setText(validationString);
        return noValid;
    }

    public static boolean phoneFormat(TextField inputTextField, Label inputLabel, String validationText) {
        boolean noValid = false;
        String validationString = null;

        try {
            Long.parseLong(inputTextField.getText());
        } catch (NumberFormatException e) {
            noValid = true;
            validationString = validationText;
        }
        inputLabel.setText(validationString);
        return noValid;
    }

    public static boolean emailFormat(TextField inputTextField, Label inputLabel, String validationText) {
        boolean noValid = false;
        String validationString = null;

//        if (!inputTextField.getText().matches("[a-z0-9](\\.?[a-z0-9]){4,}@([a-z0-9]+\\.?)+")) {
//            noValid = true;
//            validationString = validationText;
//        }
//
//        inputLabel.setText(validationString);
        return noValid;
    }

    public static boolean passFormat(TextField inputTextField, Label inputLabel, String validationText) {
        boolean isPass = false;
        String validationString = null;

        if (!inputTextField.getText().matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?!.*\\s).{8,15}")) {
            isPass = true;
            validationString = validationText;
        }
        inputLabel.setText(validationString);
        return isPass;
    }

    public static boolean confirmPassMatch(TextField inputTextField1, TextField inputTextField2, Label inputLabel, String validationText) {
        boolean isMatch = false;
        String validationString = null;

        if (!inputTextField1.getText().equals(inputTextField2.getText())) {
            isMatch = true;
            validationString = validationText;
        }
        inputLabel.setText(validationString);
        return isMatch;
    }
    public static boolean textFieldIsNull(TextField inputTextField, Label inputLabel, String validationText) {
        boolean isNull = false;
        String validationString = null;

        System.out.println("*******************************************************");

        //point out difference between null and isEmpty() *FIND OUT WHEN TO USE NULL
        if (inputTextField.getText().isEmpty()) {
            isNull = true;
            validationString = validationText;

        }
        String isEmpty = Boolean.toString(inputTextField.getText().isEmpty());
        String nil = Boolean.toString(inputTextField.getText() == null);

        inputLabel.setText(validationString);

        System.out.println("Label Should be Set to: " + validationString);
        System.out.println("Input TextField: " + inputTextField.getText());
        System.out.println("Null: " + nil + " isEmpty: " + isEmpty);

        return isNull;

    }
}
