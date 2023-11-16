package controllers.user;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
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

		dataTable.setRowFactory(dataTable -> {
			TableRow<models.File> row = new TableRow<>();
			row.setOnMouseClicked((event -> {
				if(event.getButton() == MouseButton.PRIMARY){
					if(event.getClickCount() == 1){
						if(row.getItem().getTypeId() == 1){
							currentFolderId = row.getItem().getId();
							fillData();
						}
						else {
							// Open file
						}
					}
				}
			}));
			return row;
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

	@FXML
	public void handleUploadFileButtonAction() {
		// Create a FileChooser
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a file to upload");

		// Show the file dialog and get the selected file
		File selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile != null) {
			// Get the selected file's name
			fileName = selectedFile.getName();
			String filePath = selectedFile.getAbsolutePath();

			Task<Boolean> uploadFileTask = new Task<Boolean>() {
				@Override
				protected Boolean call() throws Exception {
					ItemService itemService = new ItemService();
					System.out.println("File: " + fileName + " - " + currentFolderId + " - " + selectedFile.length() + " - " + filePath);
					boolean rs = itemService.uploadFile(fileName, 1, currentFolderId, (int)selectedFile.length(), filePath);
					return rs;
				}
			};

			uploadFileTask.setOnSucceeded(e -> {
				boolean response = uploadFileTask.getValue();
				if(response) fillData();
				else System.out.println("Upload file thành công");
			});

			uploadFileTask.setOnFailed(e -> {
				System.out.println("Upload file thất bại");
			});

			Thread thread = new Thread(uploadFileTask);
			thread.start();
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

			Task<Boolean> uploadFolderTask = new Task<Boolean>() {
				@Override
				protected Boolean call() throws Exception {
					ItemService itemService = new ItemService();
					boolean rs = itemService.uploadFolder(folderName, 1, currentFolderId, folderPath);
					return rs;
				}
			};

			uploadFolderTask.setOnSucceeded(e -> {
				boolean response = uploadFolderTask.getValue();
				if(response) fillData();
				else System.out.println("Upload folder thành công");
			});

			uploadFolderTask.setOnFailed(e -> {
				System.out.println("Upload folder thất bại");
			});

			Thread thread = new Thread(uploadFolderTask);
			thread.start();
		} else {
			System.out.println("Hãy chọn thư mục cần upload");
		}
	}

	@FXML
	public void handleOpenButtonAction(ActionEvent event) {
	}

	@FXML
	public void downloadFileClicked(ActionEvent event) {
	}

	@FXML
	public void synchronizeClicked(ActionEvent event) {
		Task<Boolean> synchronizeTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				ItemService itemService = new ItemService();
				boolean rs = itemService.synchronize(4 , currentFolderId);
				return rs;
			}
		};

		synchronizeTask.setOnSucceeded(e -> {
			boolean response = synchronizeTask.getValue();
			if(response) fillData();
			else System.out.println("Đồng bộ thành công");
		});

		synchronizeTask.setOnFailed(e -> {
			System.out.println("Đồng bộ thất bại");
		});

		Thread thread = new Thread(synchronizeTask);
		thread.start();
	}

//	private File fetchDataFromDatabase(String fileName) {
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
				Task<Boolean> createFolderTask = new Task<Boolean>() {
					@Override
					protected Boolean call() throws Exception {
						ItemService itemService = new ItemService();
						boolean rs = itemService.createFolder(folderName, 1, currentFolderId);
						return rs;
					}
				};

				createFolderTask.setOnSucceeded(e -> {
					boolean response = createFolderTask.getValue();
					if(response) fillData();
					else System.out.println("Tạo folder thất bại");
				});

				createFolderTask.setOnFailed(e -> {
					System.out.println("Tạo folder thất bại");
				});

				Thread thread = new Thread(createFolderTask);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
