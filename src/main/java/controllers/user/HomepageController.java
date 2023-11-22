package controllers.user;

import DTO.LoginSession;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.*;
import models.Type;
import models.User;
import services.client.user.ItemService;
import services.login.LoginService;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

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
	private Button searchBtn;
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
	@FXML
	private Label lbGeneral;
	@FXML
	private Label lbMyFile;
	@FXML
	private Label lbMyFileShare;
	@FXML
	private Label lbOtherFileShare;
	private int currentSideBarIndex = 0;
	List<HBox> breadcrumbList = new ArrayList<>();
    public HomepageController() {
    }

    public void populateData() {
		LoginSession loginSession = LoginService.getCurrentSession();
		String name = loginSession.getCurrentUserName();
		userName.setText(name);
		System.out.println(name);

		TableColumn<models.File, String> nameColumn = new TableColumn<>("Tên");
        TableColumn<models.File, String> ownerNameColumn = new TableColumn<>("Chủ sở hữu");
        TableColumn<models.File, Date> dateModifiedColumn = new TableColumn<>("Đã sửa đổi");
        TableColumn<models.File, String> lastModifiedByColumn = new TableColumn<>("Người sửa đổi");
        TableColumn<models.File, String> sizeColumn = new TableColumn<>("Kích thước");

        dataTable.getColumns().addAll(nameColumn, ownerNameColumn, dateModifiedColumn, lastModifiedByColumn, sizeColumn);

        nameColumn.setCellValueFactory(column -> {
            return new SimpleStringProperty(column.getValue().getName() + (column.getValue().getTypeId() != 1 ? "." + column.getValue().getTypesByTypeId().getName() : ""));
        });
		nameColumn.setCellFactory(column -> {
			return new TableCell<models.File, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(empty || item == null || getTableRow() == null ||getTableRow().getItem() == null) {
						setText(null);
						setGraphic(null);
					}
					else {
						ImageView icon = new ImageView();
						icon.setFitHeight(20);
						icon.setFitWidth(20);
						if(getTableRow().getItem().getTypeId() == 1) {
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/folder.png").toString()));
						}
						else {
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/file.png").toString()));
						}
						setGraphic(icon);
						setText(item);
					}
				}
			};
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

		breadcrumbList.clear();
		HBox breadcrumb = createBreadcrumb(2, "Chung");
		breadcrumbList.add(breadcrumb);
		// Thêm các HBox breadcrumb vào container
		path.getChildren().setAll(breadcrumbList);

		dataTable.setRowFactory(dataTable -> {
			TableRow<models.File> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if(event.getButton() == MouseButton.PRIMARY && !row.isEmpty()){
					dataTable.getSelectionModel().select(row.getIndex());
					models.File file = row.getItem();
					if(file.getTypeId() == 1){
						currentFolderId = file.getId();
						fillData();
						// Tạo HBox breadcrumb mới
						HBox _breadcrumb = createBreadcrumb(file.getId(), file.getName());
						breadcrumbList.add(_breadcrumb);
						// Thêm các HBox breadcrumb vào container
						path.getChildren().setAll(breadcrumbList);
					}
					else {
						// Open file
					}
				} else if(event.getButton() == MouseButton.SECONDARY && !row.isEmpty()){
					dataTable.getSelectionModel().select(row.getIndex());
					showOptionsPopup(event, row.getItem());
				}
			});

			row.setOnMouseEntered(event -> {
				if(!row.isEmpty() && !row.isSelected()){
					row.setStyle("-fx-background-color: #f2f2f2");
				}
			});

			row.setOnMouseExited(event -> {
				if(!row.isEmpty()){
					row.setStyle("");
				}
			});

			return row;
		});

		fillData();
    }

	private void showOptionsPopup(MouseEvent mouseEvent, models.File selectedItem) {
		Popup popup = new Popup();
		popup.setAutoHide(true);
		popup.setAutoFix(true);
		popup.setHideOnEscape(true);

		FontAwesomeIconView openIcon = new FontAwesomeIconView();
		openIcon.setGlyphName("FOLDER");
		openIcon.setSize("20");
		openIcon.setStyleClass("icon");
		Button openBtn = new Button("Mở", openIcon);

		FontAwesomeIconView downloadIcon = new FontAwesomeIconView();
		downloadIcon.setGlyphName("DOWNLOAD");
		downloadIcon.setSize("20");
		downloadIcon.setStyleClass("icon");
		Button downloadBtn = new Button("Tải xuống", downloadIcon);

		FontAwesomeIconView deleteIcon = new FontAwesomeIconView();
		deleteIcon.setGlyphName("TRASH");
		deleteIcon.setSize("20");
		deleteIcon.setStyleClass("icon");
		Button deleteBtn = new Button("Xóa", deleteIcon);

		FontAwesomeIconView renameIcon = new FontAwesomeIconView();
		renameIcon.setGlyphName("EDIT");
		renameIcon.setSize("20");
		renameIcon.setStyleClass("icon");
		Button renameBtn = new Button("Đổi tên", renameIcon);

		FontAwesomeIconView moveIcon = new FontAwesomeIconView();
		moveIcon.setGlyphName("ARROWS_ALT");
		moveIcon.setSize("20");
		moveIcon.setStyleClass("icon");
		Button moveBtn = new Button("Di chuyển", moveIcon);

		FontAwesomeIconView copyIcon = new FontAwesomeIconView();
		copyIcon.setGlyphName("COPY");
		copyIcon.setSize("20");
		copyIcon.setStyleClass("icon");
		Button copyBtn = new Button("Sao chép", copyIcon);

		FontAwesomeIconView shareIcon = new FontAwesomeIconView();
		shareIcon.setGlyphName("SHARE_ALT");
		shareIcon.setSize("20");
		shareIcon.setStyleClass("icon");
		Button shareBtn = new Button("Chia sẻ", shareIcon);

		FontAwesomeIconView syncIcon = new FontAwesomeIconView();
		syncIcon.setGlyphName("REFRESH");
		syncIcon.setSize("20");
		syncIcon.setStyleClass("icon");
		Button synchronizeBtn = new Button("Đồng bộ", syncIcon);

		openBtn.setOnAction(event -> {
			// Open file

			popup.hide();
		});

		downloadBtn.setOnAction(event -> {
			// Download file

			popup.hide();
		});

		deleteBtn.setOnAction(event -> {
			// Delete file

			popup.hide();
		});

		renameBtn.setOnAction(event -> {
			// Rename file

			popup.hide();
		});

		moveBtn.setOnAction(event -> {
			// Move file

			popup.hide();
		});

		copyBtn.setOnAction(event -> {
			// Copy file

			popup.hide();
		});

		shareBtn.setOnAction(event -> {
			// Share file

			popup.hide();
		});

		synchronizeBtn.setOnAction(event -> {
			// Synchronize file

			popup.hide();
		});

		VBox options = new VBox();
		options.setPrefWidth(150);
		options.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-radius: 15px; -fx-border-width: 1px; -fx-background-radius: 15px;");

		options.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
		for (Button button : Arrays.asList(openBtn, downloadBtn, deleteBtn, renameBtn, moveBtn, copyBtn, shareBtn, synchronizeBtn)) {
			if (button != null) {
				button.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				button.setPadding(new Insets(5, 5, 5, 15));
				button.setPrefWidth(150);

				button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
				button.setOnMouseEntered(event -> {
					button.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
				});
				button.setOnMouseExited(event -> {
					button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
				});

				if (button == openBtn) {
					button.setStyle("-fx-background-color: transparent; -fx-background-radius: 15px 15px 0px 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
					button.setOnMouseEntered(event -> {
						button.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 15px 15px 0px 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
					button.setOnMouseExited(event -> {
						button.setStyle("-fx-background-color: transparent; -fx-background-radius: 15px 15px 0px 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
				} else if(button == synchronizeBtn) {
					button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px 0px 15px 15px; -fx-background-insets: 0px; -fx-border-width: 0;");
					button.setOnMouseEntered(event -> {
						button.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 0px 0px 15px 15px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
					button.setOnMouseExited(event -> {
						button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px 0px 15px 15px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
				} else if(button == deleteBtn || button == copyBtn){
					button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px; -fx-background-insets: 0px; -fx-border-width: 0 0 1px 0; -fx-border-color: gray;");
					button.setOnMouseEntered(event -> {
						button.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 0px; -fx-background-insets: 0px; -fx-border-width: 0 0 1px 0; -fx-border-color: gray;");
					});
					button.setOnMouseExited(event -> {
						button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px; -fx-background-insets: 0px; -fx-border-width: 0 0 1px 0; -fx-border-color: gray;");
					});
				}
			}
		}

		options.getChildren().addAll(openBtn, downloadBtn, deleteBtn, renameBtn, moveBtn, copyBtn, shareBtn, synchronizeBtn);
		popup.getContent().add(options);

		popup.show(dataTable.getScene().getWindow(), mouseEvent.getScreenX(), mouseEvent.getScreenY());

		Scene scene = dataTable.getScene();
		scene.setOnMousePressed(event -> {
			Node target = (Node) event.getTarget();
			if (!popup.getScene().getRoot().getBoundsInParent().contains(event.getSceneX(), event.getSceneY())) {
				popup.hide();
			}
		});


	}

	private void fillData() {
		ItemService itemService = new ItemService();
		List<models.File> itemList = itemService.getAllItem(currentFolderId, "");

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			items.clear();
			items.addAll(itemList);

			// Tạo SortedList với Comparator để xác định thứ tự của folders và files
			SortedList<models.File> sortedData = new SortedList<>(items, (file1, file2) -> {
				if (file1.getTypeId() == 1 && file2.getTypeId() != 1) {
					return -1;
				} else if (file1.getTypeId() != 1 && file2.getTypeId() == 1) {
					return 1;
				}
				return 0;

			});

			dataTable.setItems(sortedData);
			sortedData.comparatorProperty().bind(dataTable.comparatorProperty());
			System.out.println("not null");
		}
	}

//	public void showStageWhenReady(){
//		Platform.runLater(() -> {
//			Stage stage = (Stage) dataTable.getScene().getWindow();
//			stage.show();
//		});
//	}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
		SortedList<models.File> sortedList = new SortedList<>(items, (file1, file2) -> {
			if (file1.getTypeId() == 1 && file2.getTypeId() != 1) {
				return -1;
			} else if (file1.getTypeId() != 1 && file2.getTypeId() == 1) {
				return 1;
			}
			return 0;
		});
		dataTable.comparatorProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				sortedList.setComparator((file1, file2) -> {
					if (file1.getTypeId() == 1 && file2.getTypeId() != 1) {
						return -1;
					} else if (file1.getTypeId() != 1 && file2.getTypeId() == 1) {
						return 1;
					} else {
						return newValue.compare(file1, file2);
					}
				});
			} else {
				sortedList.setComparator((file1, file2) -> {
					if (file1.getTypeId() == 1 && file2.getTypeId() != 1) {
						return -1;
					} else if (file1.getTypeId() != 1 && file2.getTypeId() == 1) {
						return 1;
					}
					return 0;
				});
			}
		});
		dataTable.setItems(sortedList);
		sortedList.comparatorProperty().bind(dataTable.comparatorProperty());
		populateData();
    }

	@FXML
	public void handleUploadFileButtonAction() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a file to upload");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

		if (selectedFiles != null) {
			for(File file : selectedFiles){
				String fileName = file.getName();
				String filePath = file.getAbsolutePath();
				System.out.println("filePath: " + filePath);
				Task<Boolean> uploadFileTask = new Task<Boolean>() {
					@Override
					protected Boolean call() throws Exception {
						ItemService itemService = new ItemService();
						boolean rs = itemService.uploadFile(fileName, 1, currentFolderId, (int) file.length(), filePath);
						return rs;
					}
				};

				uploadFileTask.setOnSucceeded(e -> {
					boolean response = uploadFileTask.getValue();
					if(response) {
						fillData();
						System.out.println("Upload file thành công");
					}
					else System.out.println("Upload file thất bại");
				});

				uploadFileTask.setOnFailed(e -> {
					System.out.println("Upload file thất bại");
				});

				Thread thread = new Thread(uploadFileTask);
				thread.start();
			}
		} else {
			System.out.println("Hãy chọn file cần upload");
		}
	}

	@FXML
	public void handleUploadFolderButtonAction() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a folder to upload");

		File selectedFolder = directoryChooser.showDialog(null);

		if (selectedFolder != null && selectedFolder.isDirectory()) {
			String folderName = selectedFolder.getName();
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
				if(response) {
					fillData();
					System.out.println("Upload folder thành công");
				}
				else System.out.println("Upload folder thất bại");
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
	public void downloadCurrentFolderClicked(ActionEvent event) {
		// Create a FileChooser to choose the path to save file
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a folder to save file");
//		directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		File selectedFolder = directoryChooser.showDialog(null);

		Task<Boolean> downloadFileTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				ItemService itemService = new ItemService();
				boolean rs = itemService.downloadFolder(selectedFolder.getAbsolutePath(), currentFolderId);
				return rs;
			}
		};

		downloadFileTask.setOnSucceeded(e -> {
			boolean response = downloadFileTask.getValue();
			if(response) fillData();
			else System.out.println("Download file thành công");
		});

		downloadFileTask.setOnFailed(e -> {
			System.out.println("Download file thất bại");
		});

		Thread thread = new Thread(downloadFileTask);
		thread.start();
	}

	@FXML
	public void synchronizeClicked(ActionEvent event) {
		Task<Boolean> synchronizeTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				ItemService itemService = new ItemService();
				boolean rs = itemService.synchronize(1 , currentFolderId);
				return rs;
			}
		};

		synchronizeTask.setOnSucceeded(e -> {
			boolean response = synchronizeTask.getValue();
			if(response) System.out.println("Đồng bộ thành công");
			else System.out.println("Đồng bộ thất bại");
		});

		synchronizeTask.setOnFailed(e -> {
			System.out.println("Đồng bộ thất bại");
		});

		Thread thread = new Thread(synchronizeTask);
		thread.start();
	}

	@FXML
	public void createFolderButtonClicked(ActionEvent event) {
		Stage popupStage = new Stage();
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setTitle("Tạo thư mục");

		popupStage.initStyle(StageStyle.UTILITY);

		BorderPane popupLayout = new BorderPane();
		popupLayout.setPadding(new Insets(10));

		Label title = new Label("Tên thư mục");
		title.setStyle("-fx-font-size: 18;");
		popupLayout.setTop(title);

		TextField folderNameTxt = new TextField();
		folderNameTxt.setPromptText("Nhập tên thư mục");
		folderNameTxt.setPrefWidth(200);
		folderNameTxt.setPrefHeight(30);
		folderNameTxt.setStyle("-fx-background-color: white");
		folderNameTxt.setStyle("-fx-border-color: gray");
		folderNameTxt.setStyle("-fx-border-width: 1px");

		popupLayout.setCenter(folderNameTxt);

		Button createBtn = new Button("Tạo");
		createBtn.setPrefWidth(100);
		createBtn.setPrefHeight(30);
		createBtn.setStyle("-fx-background-color: white");
		createBtn.setStyle("-fx-border-color: gray");
		createBtn.setStyle("-fx-border-width: 1px");

		createBtn.setOnAction(e -> {
			String foldername = folderNameTxt.getText();
			popupStage.close();

			Task<Boolean> createFolderTask = new Task<Boolean>() {
				@Override
				protected Boolean call() throws Exception {
					ItemService itemService = new ItemService();
					boolean rs = itemService.createFolder(foldername, 1, currentFolderId);
					return rs;
				}
			};

			createFolderTask.setOnSucceeded(event1 -> {
				boolean response = createFolderTask.getValue();
				if(response) fillData();
				else System.out.println("Tạo folder thành công");
			});

			createFolderTask.setOnFailed(event1 -> {
				System.out.println("Tạo folder thất bại");
			});

			Thread thread = new Thread(createFolderTask);
			thread.start();
		});

		Button cancelBtn = new Button("Hủy");
		cancelBtn.setPrefWidth(100);
		cancelBtn.setPrefHeight(30);
		cancelBtn.setStyle("-fx-background-color: white");
		cancelBtn.setStyle("-fx-border-color: gray");
		cancelBtn.setStyle("-fx-border-width: 1px");

		cancelBtn.setOnAction(e -> popupStage.close());

		HBox footerLabel = new HBox();
		footerLabel.setSpacing(10);
		footerLabel.setPadding(new Insets(10));
		footerLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
		footerLabel.getChildren().addAll(createBtn, cancelBtn);

		popupLayout.setBottom(footerLabel);

		Scene popupScene = new Scene(popupLayout, 300, 200);
		popupStage.setScene(popupScene);
		popupStage.showAndWait();
	}

	public void accessClicked(ActionEvent actionEvent) {
	}
	public void setFontLabel(int number) {
		for (int i = 0; i < 4; ++i) {
			if (i == number) continue;
			switch (i) {
				case 0:
					lbGeneral.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.REGULAR, lbGeneral.getFont().getSize()));
					break;
				case 1:
					lbMyFile.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.REGULAR, lbMyFile.getFont().getSize()));
					break;
				case 2:
					lbMyFileShare.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.REGULAR, lbMyFileShare.getFont().getSize()));
					break;
				case 3:
					lbOtherFileShare.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.REGULAR, lbOtherFileShare.getFont().getSize()));
					break;
			}
		}
	}
	public void generalPage(MouseEvent event) throws IOException {
		lbGeneral.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, lbGeneral.getFont().getSize()));
		setFontLabel(0);
		searchTxt.setText("");
		currentSideBarIndex = 0;
		currentFolderId = 2;
		breadcrumbList.clear();
		HBox breadcrumb = createBreadcrumb(2, "Chung");
		breadcrumbList.add(breadcrumb);
		// Thêm các HBox breadcrumb vào container
		path.getChildren().setAll(breadcrumbList);
		ItemService itemService = new ItemService();
		List<models.File> itemList = itemService.getAllItem(2, "");

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			final ObservableList<models.File> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}
	}
	public void myFilePage(MouseEvent event) throws IOException {
		lbMyFile.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, lbMyFile.getFont().getSize()));
		setFontLabel(1);
		searchTxt.setText("");
		currentSideBarIndex = 1;
		currentFolderId = -1;
		breadcrumbList.clear();
		HBox breadcrumb = createBreadcrumb(-1, "Tập tin của tôi");
		breadcrumbList.add(breadcrumb);
		// Thêm các HBox breadcrumb vào container
		path.getChildren().setAll(breadcrumbList);
		LoginSession loginSession = LoginService.getCurrentSession();
		int currentUserId = loginSession.getCurrentUserID();
		ItemService itemService = new ItemService();
		List<models.File> itemList = itemService.getAllItemPrivateOwnerId(currentUserId, "");

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			final ObservableList<models.File> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}
	}
	public void myShareFile(MouseEvent event) throws IOException {
		lbMyFileShare.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, lbMyFileShare.getFont().getSize()));
		setFontLabel(2);
		searchTxt.setText("");
		currentSideBarIndex = 2;
		currentFolderId = -2;
		breadcrumbList.clear();
		HBox breadcrumb = createBreadcrumb(-2, "Đã chia sẻ");
		breadcrumbList.add(breadcrumb);
		// Thêm các HBox breadcrumb vào container
		path.getChildren().setAll(breadcrumbList);
		LoginSession loginSession = LoginService.getCurrentSession();
		int currentUserId = loginSession.getCurrentUserID();
		ItemService itemService = new ItemService();
		List<models.File> itemList = itemService.getAllOtherShareItem(currentUserId, "");

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			final ObservableList<models.File> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}
	}
	public void otherFileShare(MouseEvent event) throws IOException {
		lbOtherFileShare.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, lbOtherFileShare.getFont().getSize()));
		setFontLabel(3);
		searchTxt.setText("");
		currentSideBarIndex = 3;
		currentFolderId = -3;
		breadcrumbList.clear();
		HBox breadcrumb = createBreadcrumb(-3, "Được chia sẻ");
		breadcrumbList.add(breadcrumb);
		// Thêm các HBox breadcrumb vào container
		path.getChildren().setAll(breadcrumbList);
		LoginSession loginSession = LoginService.getCurrentSession();
		int currentUserId = loginSession.getCurrentUserID();
		ItemService itemService = new ItemService();
		List<models.File> itemList = itemService.getAllSharedItem(currentUserId, "");

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			final ObservableList<models.File> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}
	}
	public void search(ActionEvent event) throws IOException {
		String txt = searchTxt.getText();
		System.out.println(txt);
		ItemService itemService = new ItemService();
		List<models.File> itemList = null;
		LoginSession loginSession = LoginService.getCurrentSession();
		int currentUserId = loginSession.getCurrentUserID();
		if (currentSideBarIndex == 0) {
			itemList = itemService.getAllItem(currentFolderId, txt);
		} else if (currentSideBarIndex == 1) {
			if (currentFolderId == -1) {
				itemList = itemService.getAllItemPrivateOwnerId(currentUserId, txt);
			} else {
				itemList = itemService.getAllItem(currentFolderId, txt);
			}
		} else if (currentSideBarIndex == 2) {
			if (currentFolderId == -2) {
				itemList = itemService.getAllOtherShareItem(currentUserId, txt);
			} else {
				itemList = itemService.getAllItem(currentFolderId, txt);
			}
		} else if (currentSideBarIndex == 3) {
			if (currentFolderId == -3) {
				itemList = itemService.getAllSharedItem(currentUserId, txt);
			} else {
				itemList = itemService.getAllItem(currentFolderId, txt);
			}
		}
		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			final ObservableList<models.File> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}
	}
	private void fillDataBreadCrumb(int index) {
		ItemService itemService = new ItemService();
		List<models.File> itemList = null;
		LoginSession loginSession = LoginService.getCurrentSession();
		int currentUserId = loginSession.getCurrentUserID();
		if (index == -1) itemList = itemService.getAllItemPrivateOwnerId(currentUserId, "");
		else if (index == -2) itemList = itemService.getAllOtherShareItem(currentUserId, "");
		else itemList = itemService.getAllSharedItem(currentUserId, "");

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			items.clear();
			items.addAll(itemList);

			// Tạo SortedList với Comparator để xác định thứ tự của folders và files
			SortedList<models.File> sortedData = new SortedList<>(items, (file1, file2) -> {
				if (file1.getTypeId() == 1 && file2.getTypeId() != 1) {
					return -1;
				} else if (file1.getTypeId() != 1 && file2.getTypeId() == 1) {
					return 1;
				}
				return 0;

			});

			dataTable.setItems(sortedData);
			sortedData.comparatorProperty().bind(dataTable.comparatorProperty());
			System.out.println("not null");
		}
	}
	// Phương thức tạo HBox breadcrumb
	private HBox createBreadcrumb(int folderId, String folderName) {
		HBox breadcrumb = new HBox();
		breadcrumb.setAlignment(Pos.CENTER_LEFT);
		breadcrumb.setSpacing(7.0);
		breadcrumb.setId(String.valueOf(folderId));
		Text folderText = new Text(folderName);
		FontAwesomeIconView angleRightIcon = new FontAwesomeIconView(FontAwesomeIcon.ANGLE_RIGHT);
		Region spacer1 = new Region();
		Region spacer2 = new Region();
		HBox.setHgrow(spacer1, Priority.ALWAYS);
		HBox.setHgrow(spacer2, Priority.ALWAYS);
		breadcrumb.getChildren().addAll(spacer1, folderText, angleRightIcon, spacer2);
		currentFolderId = folderId;
		breadcrumb.setOnMouseClicked(event -> {
			System.out.println("Clicked on Breadcrumb with ID: " + folderId);
			int clickedIndex = path.getChildren().indexOf(breadcrumb);
			if (clickedIndex != -1) {
				List<Node> keepElements = new ArrayList<>(path.getChildren().subList(0, clickedIndex + 1));
				path.getChildren().setAll(keepElements);
				List<HBox> keepBreadcrumbs = new ArrayList<>(breadcrumbList.subList(0, clickedIndex + 1));
				breadcrumbList.clear();
				breadcrumbList.addAll(keepBreadcrumbs);
				currentFolderId = folderId;
				if (currentFolderId < 0) fillDataBreadCrumb(currentFolderId);
				else fillData();
			}
		});
		return breadcrumb;
	}
}
