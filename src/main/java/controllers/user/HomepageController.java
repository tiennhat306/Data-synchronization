package controllers.user;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.Type;

import DTO.Item;
import controllers.create.NewFolderFormController;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User;
import services.user.FileService;
import services.user.ItemService;
import utils.HibernateUtil;

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
	private Button downloadBtn;
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

	public static String fileName;
	
	public static File fileFullContent;

	public static String folderName;

	private Session session;

	public HomepageController() {
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		session = sessionFactory.openSession();
	}

	public void populateData() {
		TableColumn<Item, String> nameColumn = new TableColumn<>("Tên");
		TableColumn<Item, String> ownerNameColumn = new TableColumn<>("Chủ sở hữu");
		TableColumn<Item, Date> dateModifiedColumn = new TableColumn<>("Đã sửa đổi");
		TableColumn<Item, String> lastModifiedByColumn = new TableColumn<>("Người sửa đổi");
		TableColumn<Item, String> sizeColumn = new TableColumn<>("Kích thước");

		dataTable.getColumns().addAll(nameColumn, ownerNameColumn, dateModifiedColumn, lastModifiedByColumn,
				sizeColumn);

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

		// dataTable.setItems(data);

		// log data
		// System.out.println("data: " + data);

		// log dataTable
		// System.out.println("dataTable: " + dataTable);

		ItemService itemService = new ItemService(session);
		FileService fileService = new FileService(session);
		List<Item> itemList = itemService.getAllItem(1);

		// log itemList
		System.out.println("itemList: " + itemList);

		if (itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		} else {
			final ObservableList<Item> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}

	}

	@FXML
	public void handleUploadFileButtonAction() {
	    // Create a FileChooser
	    FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Choose a file to upload");

	    // Set file extension filters if needed
	    FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
	    FileChooser.ExtensionFilter jpegFilter = new FileChooser.ExtensionFilter("JPEG files (*.jpg)", "*.jpg");
	    FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
	    FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
	    FileChooser.ExtensionFilter docxFilter = new FileChooser.ExtensionFilter("DOCX files (*.docx)", "*.docx");

	    fileChooser.getExtensionFilters().addAll(txtFilter, jpegFilter, pngFilter, pdfFilter, docxFilter);

	    // Show the file dialog and get the selected file
	    File selectedFile = fileChooser.showOpenDialog(null);

	    if (selectedFile != null) {
	        // Get the selected file's name
	        fileName = selectedFile.getName();

	        // Create a new Item with the file name and add it to the dataTable
	        Item item = new Item();
	        item.setName(fileName);
	        dataTable.getItems().add(item);

	        // Specify the destination folder where you want to upload the file
	        File uploadFolder = new File("uploads");

	        if (!uploadFolder.exists()) {
	            uploadFolder.mkdirs(); // Create the "uploads" folder if it doesn't exist
	        }

	        // Construct the destination file path in the "uploads" folder
	        String uploadedFilePath = "uploads" + File.separator + fileName;
	        File destinationFile = new File(uploadedFilePath);

	        try {
	            // Read the content of the selected file and write it to the destination
	            byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
	            Files.write(destinationFile.toPath(), fileContent);

	            // You can now use 'destinationFile' to handle the uploaded file, e.g., save its
	            // path to the database or process it further.
	            // Add your code here to perform any additional actions you need.
	        } catch (IOException e) {
	            e.printStackTrace();
	            // Handle any errors that may occur during the copy process
	        }
	    } else {
	        System.out.println("Hãy chọn file cần upload");
	    }
	}


	@FXML
	public void handleUploadFolderButtonAction() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a folder to upload");

		// Show the folder dialog and get the selected folder
		File selectedFolder = directoryChooser.showDialog(null);

		if (selectedFolder != null && selectedFolder.isDirectory()) {
			// Get the selected folder's name
			folderName = selectedFolder.getName();

			// For the TableView, you can create an Item object and add it to the dataTable
			Item item = new Item(); // Assuming you have an Item class
			item.setName(folderName);
			dataTable.getItems().add(item);

			// Specify the destination folder where you want to upload the folder
			File uploadFolder = new File("uploads" + File.separator + folderName);

			if (!uploadFolder.exists()) {
				uploadFolder.mkdirs(); // Create the destination folder if it doesn't exist
			}

			// Call a method to upload the entire folder and its contents
			uploadFolderContents(selectedFolder, uploadFolder);
		} else {
			System.out.println("Hãy chọn thư mục cần upload");
		}
	}
	
	@FXML
	public void downloadFileClicked(ActionEvent event) {
	    // Get the selected item from the DataTable
	    Item selectedItem = dataTable.getSelectionModel().getSelectedItem();

	    if (selectedItem != null) {
	        // Assuming you have a method to retrieve data from the database
	        String data = fetchDataFromDatabase(selectedItem.getId()); // Replace with your data retrieval logic

	        if (data != null && !data.isEmpty()) {
	            try {
	                File downloadDirectory = new File(System.getProperty("user.home") + File.separator + "Downloads");
	                if (!downloadDirectory.exists()) {
	                    downloadDirectory.mkdirs();
	                }

	                // Create a file with a unique name
	                String fileName = selectedItem.getName();
	                File downloadFile = new File(downloadDirectory, fileName);

	                try (PrintWriter writer = new PrintWriter(downloadFile)) {
	                    writer.write(data);
	                }

	                System.out.println("Data downloaded successfully to the default downloads directory.");
	            } catch (IOException e) {
	                e.printStackTrace();
	                // Handle any errors that may occur during the download process
	            }
	        } else {
	            System.out.println("No data found for the selected item.");
	        }
	    } else {
	        System.out.println("No item selected in the DataTable.");
	    }
	}

	private String fetchDataFromDatabase(int itemId) {
	    // Replace this with your database retrieval logic
	    // Return the data as a string
	    return "Data retrieved from the database for Item " + itemId;
	}


	@FXML
	public void createFolderButtonClicked(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/create/createfolderform.fxml"));
			Parent root = loader.load();
			NewFolderFormController controller = loader.getController();

			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.showAndWait();

			folderName = controller.getFolderName();

			if (folderName != null && !folderName.isEmpty()) {
				// Create a new folder with the provided name inside the "uploads" directory
				File uploadFolder = new File("uploads");

				if (!uploadFolder.exists()) {
					uploadFolder.mkdirs(); // Create the "uploads" folder if it doesn't exist
				}

				// Construct the destination folder path in the "uploads" directory
				String destinationFolderPath = "uploads" + File.separator + folderName;
				File destinationFolder = new File(destinationFolderPath);

				if (!destinationFolder.exists()) {
					destinationFolder.mkdirs(); // Create a folder with the same name in "uploads"
				}

				// Add the newly created folder to your dataTable or take any other necessary
				// actions
				Item item = new Item();
				item.setName(folderName);
				dataTable.getItems().add(item);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Recursive method to upload folder and its contents
	private void uploadFolderContents(File sourceFolder, File destinationFolder) {
		if (sourceFolder.isDirectory()) {
			if (!destinationFolder.exists()) {
				destinationFolder.mkdirs(); // Create the destination folder if it doesn't exist
			}

			String[] files = sourceFolder.list();
			if (files != null) {
				for (String file : files) {
					File srcFile = new File(sourceFolder, file);
					File destFile = new File(destinationFolder, file);

					uploadFolderContents(srcFile, destFile); // Recursively copy subfolders and files
				}
			}
		} else {
			try {
				Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
				// You can now use 'destinationFolder' to handle the uploaded file, e.g., save
				// its path to the database or process it further.
			} catch (IOException e) {
				e.printStackTrace();
				// Handle any errors that may occur during the copy process
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		populateData();
	}
}
