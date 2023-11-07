package controllers.user;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Type;
import models.User;
import services.client.user.ItemService;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
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
	private Button downloadBtn;
	@FXML
	private TableView<models.File> dataTable;
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

	private int currentFolderId = 2;
	private ObservableList<models.File> items = FXCollections.observableArrayList();

	private String fileName;
	
	public static File fileFullContent;

	private String folderName;


    public HomepageController() {
    }

    public void populateData() {
        TableColumn<models.File, String> nameColumn = new TableColumn<>("Tên");
        TableColumn<models.File, String> ownerNameColumn = new TableColumn<>("Chủ sở hữu");
        TableColumn<models.File, Date> dateModifiedColumn = new TableColumn<>("Đã sửa đổi");
        TableColumn<models.File, String> lastModifiedByColumn = new TableColumn<>("Người sửa đổi");
        TableColumn<models.File, String> sizeColumn = new TableColumn<>("Kích thước");

        dataTable.getColumns().addAll(nameColumn, ownerNameColumn, dateModifiedColumn, lastModifiedByColumn, sizeColumn);

        nameColumn.setCellValueFactory(column -> {
            return new SimpleStringProperty(column.getValue().getName() + (column.getValue().getTypeId() != 1 ? "." + column.getValue().getTypesByTypeId().getName() : ""));
        });
        ownerNameColumn.setCellValueFactory(column -> {
            return new SimpleStringProperty(column.getValue().getUsersByOwnerId().getName());
        });
        dateModifiedColumn.setCellFactory(column -> {
            return new TableCell<models.File, Date>() {
                private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item == null) {
                        setText(null);
                    }
                    else {
                        setText(format.format(item));
                    }
                }
            };
        });
        dateModifiedColumn.setCellValueFactory(new PropertyValueFactory<models.File, Date>("updatedAt"));
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

		fillData();
    }

	private void fillData() {
		ItemService itemService = new ItemService();
		List<models.File> itemList = itemService.getAllItem(currentFolderId);

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			items.clear();
			items.addAll(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}
	}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateData();
    }

//	public void sendFileNameToServer() {
//		if (fileName != null) {
//			ItemService itemService = new ItemService();
//			boolean rs = itemService.uploadItem(fileName);
//			if(rs) fillData();
//		} else {
//			System.out.println("Không có fileName nào để gửi.");
//		}
//	}

//	public void sendFolderNameToServer() {
//		if (folderName != null) {
//			ItemService itemService = new ItemService();
//			boolean rs = itemService.uploadItem(folderName);
//			if(rs) fillData();
//		} else {
//			System.out.println("Không có folderName nào để gửi.");
//		}
//	}


	@FXML
	public void handleUploadFileButtonAction() {
		// Create a FileChooser
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a file to upload");

//		// Set file extension filters if needed
//		FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
//		FileChooser.ExtensionFilter jpegFilter = new FileChooser.ExtensionFilter("JPEG files (*.jpg)", "*.jpg");
//		FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
//		FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
//		FileChooser.ExtensionFilter docxFilter = new FileChooser.ExtensionFilter("DOCX files (*.docx)", "*.docx");
//
//		fileChooser.getExtensionFilters().addAll(txtFilter, jpegFilter, pngFilter, pdfFilter, docxFilter);

		// Show the file dialog and get the selected file
		File selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile != null) {
			// Get the selected file's name
			fileName = selectedFile.getName();
			String filePath = selectedFile.getAbsolutePath();

			// Create a new Item with the file name and add it to the dataTable
//			models.File item = new models.File();
//			String SERVER_PATH = "D:\\User\\Desktop\\Server";
//			item.setName(fileName);
//			dataTable.getItems().add(item);

			// Specify the destination folder where you want to upload the file
//			File uploadFolder = new File(SERVER_PATH);
//
//			if (!uploadFolder.exists()) {
//				uploadFolder.mkdirs(); // Create the "uploads" folder if it doesn't exist
//			}

			// Construct the destination file path in the "uploads" folder
//			String uploadedFilePath = SERVER_PATH + File.separator + fileName;
//			File destinationFile = new File(uploadedFilePath);

			// Create a Task to upload and save the file
//			Task<Void> uploadTask = new Task<Void>() {
//				@Override
//				protected Void call() {
//					ItemService itemService = new ItemService();
//					System.out.println("File: " + fileName + " - " + currentFolderId + " - " + selectedFile.length() + " - " + filePath);
//					boolean rs = itemService.uploadFile(fileName, 1, currentFolderId, (int)selectedFile.length(), filePath);
//					if(rs) fillData();
//					fillData();
//					return null;
//				}
//			};
//
//			// Set up task completion handling
//			uploadTask.setOnSucceeded(event -> {
//				System.out.println("File uploaded and saved.");
//				// Add any UI update code here if needed.
//			});
//
//			uploadTask.setOnFailed(event -> {
//				System.out.println("Failed to upload and save the file.");
//				// Add error handling code here if needed.
//			});
//
//			// Start the task in a new thread
//			Thread uploadThread = new Thread(uploadTask);
//			uploadThread.setDaemon(true);
//			uploadThread.start();

			ItemService itemService = new ItemService();
			System.out.println("File: " + fileName + " - " + currentFolderId + " - " + selectedFile.length() + " - " + filePath);
			boolean rs = itemService.uploadFile(fileName, 1, currentFolderId, (int)selectedFile.length(), filePath);
			if(rs) fillData();
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
			String folderPath = selectedFolder.getAbsolutePath();

			// For the TableView, you can create an Item object and add it to the dataTable
//			models.File item = new models.File();
//			String SERVER_PATH = "D:\\User\\Desktop\\Server";
//			item.setName(folderName);
//			dataTable.getItems().add(item);

			// Specify the destination folder where you want to upload the folder
//			File uploadFolder = new File(SERVER_PATH + File.separator + folderName);
//
//			if (!uploadFolder.exists()) {
//				uploadFolder.mkdirs(); // Create the destination folder if it doesn't exist
//			}

			// Call a method to upload the entire folder and its contents
			//uploadFolderContents(selectedFolder, uploadFolder);

			// Create a Task to upload and save the file
//			Task<Void> uploadTask = new Task<Void>() {
//				@Override
//				protected Void call() {
//					ItemService itemService = new ItemService();
//					boolean response = itemService.uploadFolder(folderName, 1, currentFolderId, folderPath);
//					if(response) fillData();
//					return null;
//				}
//			};
//
//			// Set up task completion handling
//			uploadTask.setOnSucceeded(event -> {
//				System.out.println("Folder uploaded and saved.");
//				// Add any UI update code here if needed.
//			});
//
//			uploadTask.setOnFailed(event -> {
//				System.out.println("Failed to upload and save the folder.");
//				// Add error handling code here if needed.
//			});
//
//			// Start the task in a new thread
//			Thread uploadThread = new Thread(uploadTask);
//			uploadThread.setDaemon(true);
//			uploadThread.start();

			ItemService itemService = new ItemService();
			boolean response = itemService.uploadFolder(folderName, 1, currentFolderId, folderPath);
			if(response) fillData();
		} else {
			System.out.println("Hãy chọn thư mục cần upload");
		}
	}

	@FXML
	public void handleOpenButtonAction(ActionEvent event) {
//		// Get the selected item from the DataTable
//		models.File selectedItem = dataTable.getSelectionModel().getSelectedItem();
//
//		if (selectedItem != null) {
//			// Get the file name associated with the selected item
//			String fileName = selectedItem.getName();
//			// Construct the file path
//			File data = fetchDataFromDatabase(selectedItem.getName().toString());
//			// Use the custom method to open the file
//			try {
//				Desktop.getDesktop().open(data);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		} else {
//			System.out.println("No item selected in the DataTable.");
//		}
	}

	@FXML
	public void downloadFileClicked(ActionEvent event) {
		// Get the selected item from the DataTable
//		models.File selectedItem = dataTable.getSelectionModel().getSelectedItem();
//
//		if (selectedItem != null) {
//			// Assuming you have a method to retrieve data from the database
//			File data = fetchDataFromDatabase(selectedItem.getName().toString());
//
//			if (data != null) {
//				try {
//					// Get the default "Downloads" directory in the user's home folder
//					File downloadDirectory = new File(System.getProperty("user.home") + File.separator + "Downloads");
//
//					if (!downloadDirectory.exists()) {
//						downloadDirectory.mkdirs();
//					}
//
//					// Create a file with a unique name in the "Downloads" directory
//					String fileName = selectedItem.getName(); // Replace "extension" with the actual file extension
//					File downloadFile = new File(downloadDirectory, fileName);
//
//					// Use FileInputStream and FileOutputStream to copy the file content
//					try (FileInputStream inputStream = new FileInputStream(data);
//						 FileOutputStream outputStream = new FileOutputStream(downloadFile)) {
//						byte[] buffer = new byte[1024];
//						int length;
//						while ((length = inputStream.read(buffer)) > 0) {
//							outputStream.write(buffer, 0, length);
//						}
//					}
//
//					System.out.println("Data downloaded successfully to the default downloads directory.");
//				} catch (IOException e) {
//					e.printStackTrace();
//					// Handle any errors that may occur during the download process
//				}
//			} else {
//				System.out.println("No data found for the selected item.");
//			}
//		} else {
//			System.out.println("No item selected in the DataTable.");
//		}
	}


//	private File fetchDataFromDatabase(String fileName) {
//		String SERVER_PATH = "D:\\User\\Desktop\\Server";
//		String path = SERVER_PATH;
//
//		// Define the source path where the file is stored on the server
//		String sourceFilePath = path + File.separator + fileName;
//
//		File sourceFile = new File(sourceFilePath);
//
//		if(sourceFile.exists()) {
//			return sourceFile;
//		}
//
//		return null;
//	}

	@FXML
	public void createFolderButtonClicked(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user/create_folder_form.fxml"));
			Parent root = loader.load();
			NewFolderFormController controller = loader.getController();

			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.showAndWait();

			folderName = controller.getFolderName();

			if (folderName != null && !folderName.isEmpty()) {
//				// Create a new folder with the provided name inside the "uploads" directory
//				String SERVER_PATH = "D:\\User\\Desktop\\Server";
//				File uploadFolder = new File(SERVER_PATH);
//
//				if (!uploadFolder.exists()) {
//					uploadFolder.mkdirs(); // Create the "uploads" folder if it doesn't exist
//				}
//
//				// Construct the destination folder path in the "uploads" directory
//				String destinationFolderPath = SERVER_PATH + File.separator + folderName;
//				File destinationFolder = new File(destinationFolderPath);
//
//				if (!destinationFolder.exists()) {
//					destinationFolder.mkdirs(); // Create a folder with the same name in "uploads"
//				}

//				// Add the newly created folder to your dataTable or take any other necessary actions
//				models.File item = new models.File();
//				item.setName(folderName);
//				dataTable.getItems().add(item);

				// Create a Task to upload and save the folder
//				Task<Void> uploadTask = new Task<Void>() {
//					@Override
//					protected Void call() {
//						ItemService itemService = new ItemService();
//						boolean rs = itemService.createFolder(folderName, 1, currentFolderId);
//						return null;
//					}
//				};
//
//				// Set up task completion handling
//				uploadTask.setOnSucceeded(uploadEvent -> {
//					System.out.println("Folder uploaded and saved.");
//					// Add any UI update code here if needed.
//				});
//
//				uploadTask.setOnFailed(failedEvent -> {
//					System.out.println("Failed to upload and save the folder.");
//					// Add error handling code here if needed.
//				});
//
//				// Start the task in a new thread
//				Thread uploadThread = new Thread(uploadTask);
//				uploadThread.setDaemon(true);
//				uploadThread.start();
				ItemService itemService = new ItemService();
				boolean rs = itemService.createFolder(folderName, 1, currentFolderId);
				if(rs) fillData();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Recursive method to upload folder and its contents
//	private void uploadFolderContents(File sourceFolder, File destinationFolder) {
//		if (sourceFolder.isDirectory()) {
//			if (!destinationFolder.exists()) {
//				destinationFolder.mkdirs(); // Create the destination folder if it doesn't exist
//			}
//
//			String[] files = sourceFolder.list();
//			if (files != null) {
//				for (String file : files) {
//					File srcFile = new File(sourceFolder, file);
//					File destFile = new File(destinationFolder, file);
//
//					uploadFolderContents(srcFile, destFile); // Recursively copy subfolders and files
//				}
//			}
//		} else {
//			try {
//				Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
//				// You can now use 'destinationFolder' to handle the uploaded file, e.g., save
//				// its path to the database or process it further.
//			} catch (IOException e) {
//				e.printStackTrace();
//				// Handle any errors that may occur during the copy process
//			}
//		}
//	}
}
