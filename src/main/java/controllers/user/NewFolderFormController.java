package controllers.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewFolderFormController {

	@FXML
    public TextField folderNameField;
	@FXML
	public String folderName;
    @FXML
    private Button createButton;
    
    public String getFolderName() {
        return folderName;
    }

    @FXML
    public void createFolder(ActionEvent event) {
        folderName = folderNameField.getText();
        // Close the forms
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
