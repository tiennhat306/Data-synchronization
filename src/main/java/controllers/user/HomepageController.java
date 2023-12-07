package controllers.user;

import DTO.LoginSession;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import models.Folder;
import models.RecentFile;
import models.Type;
import models.User;
import services.client.user.ItemService;
import services.client.user.PermissionService;
import services.login.LoginService;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class HomepageController implements Initializable {
	@FXML
	private Label lbRecentOpenBtn;
	@FXML
	private Label lbTrashBtn;
	@FXML
	private HBox HistoryBtn;
	@FXML
	private HBox SharedBtn;
	@FXML
	private HBox SharedByOtherBtn;
	@FXML
	private Button createFolderBtn;
	@FXML
	private Button downloadBtn;
	@FXML
	private TableView<Object> dataTable;
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
	private final int userId;
    public HomepageController() {
		userId = LoginService.getCurrentSession().getCurrentUserID();
    }

    public void populateData() {
		LoginSession loginSession = LoginService.getCurrentSession();
		String name = loginSession.getCurrentUserName();
		userName.setText(name);

		TableColumn<Object, String> nameColumn = new TableColumn<>("Tên");
        TableColumn<Object, String> ownerNameColumn = new TableColumn<>("Chủ sở hữu");
        TableColumn<Object, Date> dateModifiedColumn = new TableColumn<>("Đã sửa đổi");
        TableColumn<Object, String> lastModifiedByColumn = new TableColumn<>("Người sửa đổi");
        TableColumn<Object, String> sizeColumn = new TableColumn<>("Kích thước");

        dataTable.getColumns().addAll(nameColumn, ownerNameColumn, dateModifiedColumn, lastModifiedByColumn, sizeColumn);

        nameColumn.setCellValueFactory(column -> {
            return new SimpleStringProperty(((models.File)column.getValue()).getName() + (((models.File)column.getValue()).getTypeId() != 1 ? "." + ((models.File)column.getValue()).getTypesByTypeId().getName() : ""));
        });
		nameColumn.setCellFactory(column -> {
			return new TableCell<Object, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(empty || item == null || getTableRow() == null ||((models.File)getTableRow().getItem()) == null) {
						setText(null);
						setGraphic(null);
					}
					else {
						ImageView icon = new ImageView();
						icon.setFitHeight(20);
						icon.setFitWidth(20);
						if(((models.File)getTableRow().getItem()).getTypeId() == 1){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/folder.png").toString()));
						} else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().equals("txt")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/txt.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().matches("docx?|docm|dotx?|dotm")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/doc.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().equals("pdf")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/pdf.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().matches("mp4|mp3|avi|flv|wmv|mov|wav|wma|ogg|mkv")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/mp4.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().matches("png|svg|jpg|jpeg|gif|bmp")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/picture.png").toString()));
						}
						else {
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/unknown.png").toString()));
						}


						setGraphic(icon);
						setText(item);
					}
				}
			};
		});

        ownerNameColumn.setCellValueFactory(column -> {
            return new SimpleStringProperty(((models.File)column.getValue()).getUsersByOwnerId().getName() == null ? "" : ((models.File)column.getValue()).getUsersByOwnerId().getName());
        });
        dateModifiedColumn.setCellFactory(column -> {
            return new TableCell<Object, Date>() {
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
        dateModifiedColumn.setCellValueFactory(new PropertyValueFactory<Object, Date>("updatedAt"));
        lastModifiedByColumn.setCellValueFactory(column -> {
            return new SimpleStringProperty(((models.File)column.getValue()).getUsersByUpdatedBy() == null ? "" : ((models.File)column.getValue()).getUsersByUpdatedBy().getName());
        });
        sizeColumn.setCellValueFactory(column -> {
            int size = ((models.File)column.getValue()).getSize();
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
			TableRow<Object> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if(event.getButton() == MouseButton.PRIMARY && !row.isEmpty()){
					dataTable.getSelectionModel().select(row.getIndex());
					models.File file = ((models.File)row.getItem());
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
					showOptionsPopup(event, ((models.File)row.getItem()));
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

		dataTable.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/assets/css/tableview.css")).toExternalForm());

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

		FontAwesomeIconView accessIcon = new FontAwesomeIconView();
		accessIcon.setGlyphName("SHARE_SQUARE");
		accessIcon.setSize("20");
		accessIcon.setStyleClass("icon");
		Button accessBtn = new Button("Quyền truy cập", accessIcon);

		FontAwesomeIconView syncIcon = new FontAwesomeIconView();
		syncIcon.setGlyphName("REFRESH");
		syncIcon.setSize("20");
		syncIcon.setStyleClass("icon");
		Button synchronizeBtn = new Button("Đồng bộ", syncIcon);

		openBtn.setOnAction(event -> {
			// Open file
			int itemTypeId = selectedItem.getTypeId();
			int itemId = selectedItem.getId();

			Task<String> openTask = new Task<String>() {
				@Override
				protected String call() throws Exception {
					ItemService itemService = new ItemService();
					if(itemTypeId == 1) {
						return itemService.openFolder(userId, itemId);
					} else {
						return itemService.openFile(userId, itemId);
					}
				}
			};

			openTask.setOnSucceeded(e -> {
				String path = openTask.getValue();
				if(path != null && !path.isEmpty()) {
					// Open file
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(new File(path));
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
					System.out.println("Mở thành công");
				}
				else System.out.println("Mở thất bại");
			});

			openTask.setOnFailed(e -> {
				System.out.println("Mở thất bại");
			});

			Thread thread = new Thread(openTask);
			thread.start();

			popup.hide();
		});

		downloadBtn.setOnAction(event -> {
			// Download file
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Choose a folder to save file");

			File selectedFolder = directoryChooser.showDialog(null);

			Task<Boolean> downloadFileTask = new Task<Boolean>() {
				@Override
				protected Boolean call() throws Exception {
					ItemService itemService = new ItemService();
					boolean rs = false;
					int itemTypeId = selectedItem.getTypeId();
					int itemId = selectedItem.getId();
					if(selectedFolder != null) {
						String path = selectedFolder.getAbsolutePath();
						if(itemTypeId == 1) {
							rs = itemService.downloadFolder(path, userId, itemId);
						}
						else {
							rs = itemService.downloadFile(path, itemId);
						}
					}
					return rs;
				}
			};

			downloadFileTask.setOnSucceeded(e -> {
				boolean response = downloadFileTask.getValue();
				if(response) System.out.println("Download file thành công");
				else System.out.println("Download file thất bại");
			});

			downloadFileTask.setOnFailed(e -> {
				System.out.println("Download file thất bại");
			});

			Thread thread = new Thread(downloadFileTask);
			thread.start();

			popup.hide();
		});

		deleteBtn.setOnAction(event -> {
			// Delete file
			int itemTypeId = selectedItem.getTypeId();
			int itemId = selectedItem.getId();

			sendDeleteRequest(itemTypeId, itemId);

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
			int itemTypeId = selectedItem.getTypeId();
			int itemId = selectedItem.getId();

			showSharePopup(itemTypeId, itemId);

			popup.hide();
		});

		synchronizeBtn.setOnAction(event -> {
			// Synchronize file

			int itemTypeId = selectedItem.getTypeId();
			int itemId = selectedItem.getId();
			Task<Boolean> synchronizeTask = new Task<Boolean>() {
				@Override
				protected Boolean call() throws Exception {
					ItemService itemService = new ItemService();
					if(itemTypeId == 1) {
						boolean rs = itemService.synchronizeFolder(userId, itemId);
						return rs;
					} else {
						boolean rs = itemService.synchronizeFile(userId, itemId);
						return rs;
					}
				}
			};

			synchronizeTask.setOnSucceeded(e -> {
				boolean response = synchronizeTask.getValue();
				if(response) {
					System.out.println("Đồng bộ thành công");
				}
				else System.out.println("Đồng bộ thất bại");
			});

			synchronizeTask.setOnFailed(e -> {
				System.out.println("Đồng bộ thất bại");
			});

			Thread thread = new Thread(synchronizeTask);
			thread.start();

			popup.hide();
		});

		VBox options = new VBox();
		options.setPrefWidth(150);
		options.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-radius: 15px; -fx-border-width: 1px; -fx-background-radius: 15px;");

		options.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
		for (Button button : Arrays.asList(openBtn, downloadBtn, deleteBtn, renameBtn, moveBtn, copyBtn, shareBtn, accessBtn, synchronizeBtn)) {
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
				} else if(button == downloadBtn || button == copyBtn){
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

		options.getChildren().addAll(openBtn, downloadBtn, shareBtn, synchronizeBtn);

		Task<Integer> checkPermissionTask = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				PermissionService permissionService = new PermissionService();
				return permissionService.checkPermission(userId, selectedItem.getTypeId(), selectedItem.getId());
			}
		};

		checkPermissionTask.setOnSucceeded(e -> {
			int permissionType = checkPermissionTask.getValue();

			if(permissionType == 3) {
				options.getChildren().add(2, deleteBtn);
				options.getChildren().add(3, renameBtn);
				options.getChildren().add(4, moveBtn);
				options.getChildren().add(5, copyBtn);
			}

		});

		checkPermissionTask.setOnFailed(e -> {
			System.out.println("Lỗi khi kiểm tra quyền truy cập");
		});

		Thread thread = new Thread(checkPermissionTask);
		thread.start();

		accessBtn.setOnAction(event -> {
			int itemTypeId = selectedItem.getTypeId();
			int itemId = selectedItem.getId();

			showAccessPopup(itemTypeId, itemId);

			popup.hide();
		});

		if(userId == selectedItem.getOwnerId()) {
			options.getChildren().add(options.getChildren().size() - 1, accessBtn);
		}
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

	private void sendDeleteRequest(int itemTypeId, int itemId) {
		Task<Boolean> deleteTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				ItemService itemService = new ItemService();
				boolean rs = itemService.deleteItem(itemTypeId, itemId, userId);
				return rs;
			}
		};

		deleteTask.setOnSucceeded(e -> {
			boolean response = deleteTask.getValue();
			if(response) {
				fillData();
				System.out.println("Xóa thành công");
			}
			else System.out.println("Xóa thất bại");
		});

		deleteTask.setOnFailed(e -> {
			System.out.println("Xóa thất bại");
		});

		Thread thread = new Thread(deleteTask);
		thread.start();
	}
	private void sendDeletePermanentlyRequest(int itemTypeId, int itemId) {
		Task<Boolean> deleteTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				ItemService itemService = new ItemService();
				boolean rs = itemService.deleteItemPermanently(itemTypeId, itemId);
				return rs;
			}
		};

		deleteTask.setOnSucceeded(e -> {
			boolean response = deleteTask.getValue();
			if(response) {
				fillDeletedData();
				System.out.println("Xóa thành công");
			}
			else System.out.println("Xóa thất bại");
		});

		deleteTask.setOnFailed(e -> {
			System.out.println("Xóa thất bại");
		});

		Thread thread = new Thread(deleteTask);
		thread.start();
	}

	private void sendRestoreRequest(int itemTypeId, int itemId) {
		Task<Boolean> restoreTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				ItemService itemService = new ItemService();
				boolean rs = itemService.restore(itemTypeId, itemId);
				return rs;
			}
		};

		restoreTask.setOnSucceeded(e -> {
			boolean response = restoreTask.getValue();
			if(response) {
				fillDeletedData();
				System.out.println("Khôi phục thành công");
			}
			else System.out.println("Khôi phục thất bại");
		});

		restoreTask.setOnFailed(e -> {
			System.out.println("Khôi phục thất bại");
		});

		Thread thread = new Thread(restoreTask);
		thread.start();
	}
	private void fillData() {
		ItemService itemService = new ItemService();
		List<models.File> itemList = itemService.getAllItem(userId, currentFolderId, "");

		if(itemList == null) {
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			items.clear();
			items.addAll(itemList);

			// Tạo SortedList với Comparator để xác định thứ tự của folders và files
			SortedList<Object> sortedData = new SortedList<>(items, (file1, file2) -> {
				if (((models.File)file1).getTypeId() == 1 && ((models.File)file2).getTypeId() != 1) {
					return -1;
				} else if (((models.File)file1).getTypeId() != 1 && ((models.File)file2).getTypeId() == 1) {
					return 1;
				}
				return 0;

			});

			dataTable.setItems(sortedData);
			sortedData.comparatorProperty().bind(dataTable.comparatorProperty());
		}

		PermissionService permissionService = new PermissionService();
		int permissionType = permissionService.checkPermission(userId, 1, currentFolderId);
		if(permissionType == 3) {
			createFolderBtn.setDisable(false);
			uploadFileBtn.setDisable(false);
			uploadFolderBtn.setDisable(false);
		}
		else {
			createFolderBtn.setDisable(true);
			uploadFileBtn.setDisable(true);
			uploadFolderBtn.setDisable(true);
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
		populateData();

		SortedList<Object> sortedList = new SortedList<>(items, (file1, file2) -> {
			if (((models.File)file1).getTypeId() == 1 && ((models.File)file2).getTypeId() != 1) {
				return -1;
			} else if (((models.File)file1).getTypeId() != 1 && ((models.File)file2).getTypeId() == 1) {
				return 1;
			}
			return 0;
		});
		dataTable.comparatorProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				sortedList.setComparator((file1, file2) -> {
					if (((models.File)file1).getTypeId() == 1 && ((models.File)file2).getTypeId() != 1) {
						return -1;
					} else if (((models.File)file1).getTypeId() != 1 && ((models.File)file2).getTypeId() == 1) {
						return 1;
					} else {
						return newValue.compare(file1, file2);
					}
				});
			} else {
				sortedList.setComparator((file1, file2) -> {
					if (((models.File)file1).getTypeId() == 1 && ((models.File)file2).getTypeId() != 1) {
						return -1;
					} else if (((models.File)file1).getTypeId() != 1 && ((models.File)file2).getTypeId() == 1) {
						return 1;
					}
					return 0;
				});
			}
		});
		dataTable.setItems(sortedList);
		sortedList.comparatorProperty().bind(dataTable.comparatorProperty());
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
				Task<Boolean> uploadFileTask = new Task<Boolean>() {
					@Override
					protected Boolean call() throws Exception {
						ItemService itemService = new ItemService();
						boolean rs = itemService.uploadFile(fileName, userId, currentFolderId, (int) file.length(), filePath);
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
					boolean rs = itemService.uploadFolder(folderName, userId, currentFolderId, folderPath);
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

	public void showSharePopup(int itemTypeId, int itemId) {
		Stage shareStage = new Stage();
		shareStage.initModality(Modality.APPLICATION_MODAL);
		shareStage.setTitle("Chia sẻ");

		shareStage.initStyle(StageStyle.UTILITY);

		ArrayList<Integer> userIds = new ArrayList<>();

		BorderPane shareLayout = new BorderPane();
		shareLayout.setPadding(new Insets(10));

		HBox topContainer = new HBox();
		topContainer.setSpacing(10);
		topContainer.setPadding(new Insets(10));

		Label title = new Label("Chia sẻ");
		title.setStyle("-fx-font-size: 18;");

		ComboBox<String> permissionCbb = new ComboBox<>();

		permissionCbb.getItems().addAll("Chỉ xem", "Chỉnh sửa");

		final int[] ownerId = {-1};
		Task<Integer> getPermissionTask = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				PermissionService permissionService = new PermissionService();
				int permission = permissionService.getPermission(itemTypeId, itemId);
				ownerId[0] = permissionService.getOwnerId(itemTypeId, itemId);
				return permission;
			}
		};

		getPermissionTask.setOnSucceeded(e -> {
			int permission = getPermissionTask.getValue();
			if(permission == 2) {
				permissionCbb.setValue("Chỉ xem");
				permissionCbb.setDisable(true);

				if(userId == ownerId[0]) {
					permissionCbb.setDisable(false);
				}
			}
			else if(permission == 3){
				permissionCbb.setValue("Chỉnh sửa");
				permissionCbb.setDisable(false);
			}
			else if(userId == ownerId[0]) {
				permissionCbb.setValue("Chỉ xem");
				permissionCbb.setDisable(false);
			}
			else {
				shareStage.close();
			}
		});

		getPermissionTask.setOnFailed(e -> {
			System.out.println("Lỗi khi lấy quyền truy cập");
		});

		Thread thread1 = new Thread(getPermissionTask);
		thread1.start();

		permissionCbb.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1px; -fx-background-radius: 15px;");
		permissionCbb.setPrefWidth(100);
		permissionCbb.setPrefHeight(30);
		permissionCbb.setPadding(new Insets(5));

		topContainer.getChildren().addAll(title, permissionCbb);
		shareLayout.setTop(topContainer);


		VBox centerContainer = new VBox();
		centerContainer.setSpacing(10);
		centerContainer.setPadding(new Insets(10));
		centerContainer.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1px; -fx-background-radius: 15px;");

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(centerContainer);
		scrollPane.setFitToWidth(true);
		scrollPane.setPadding(new Insets(10));


		TextField shareTxt = new TextField();
		shareTxt.setPromptText("Nhập tên hoặc email người dùng");
		shareTxt.setPrefWidth(200);
		shareTxt.setPrefHeight(30);
		shareTxt.setStyle("-fx-background-color: white");
		shareTxt.setStyle("-fx-border-color: gray");
		shareTxt.setStyle("-fx-border-width: 1px");
		shareTxt.setDisable(true);

		centerContainer.getChildren().add(shareTxt);

		VBox sharedContainer = new VBox();
		sharedContainer.setSpacing(5);
		sharedContainer.setPadding(new Insets(10));
		sharedContainer.setStyle("-fx-background-color: white;");

		Label sharedTitle = new Label("Đã chia sẻ");
		sharedTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
		sharedContainer.getChildren().add(sharedTitle);

		VBox userSelectedContainer = new VBox();
		userSelectedContainer.setSpacing(10);
		userSelectedContainer.setPadding(new Insets(10));
		userSelectedContainer.setStyle("-fx-background-color: white;");
		Label userSelectedTitle = new Label("Người dùng được chọn");
		userSelectedTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
		userSelectedContainer.getChildren().add(userSelectedTitle);

		VBox userContainer = new VBox();
		userContainer.setSpacing(10);
		userContainer.setPadding(new Insets(10));
		userContainer.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
		Label userTitle = new Label("Danh sách tìm kiếm");
		userTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
		userContainer.getChildren().add(userTitle);

		Task<List<User>> getSharedUserTask = new Task<List<User>>() {
			@Override
			protected List<User> call() throws Exception {
				ItemService itemService = new ItemService();
				List<User> userList = itemService.getSharedUser(itemTypeId, itemId);
				return userList;
			}
		};

		getSharedUserTask.setOnSucceeded(event -> {
			List<User> userList = getSharedUserTask.getValue();
			if(userList != null && userList.size() > 0) {
				for (User user : userList) {
					HBox userBox = new HBox();
					userBox.setSpacing(10);
					userBox.setAlignment(Pos.CENTER_LEFT);

					FontAwesomeIconView userIcon = new FontAwesomeIconView();
					userIcon.setGlyphName("USER");
					userIcon.setSize("20");
					userIcon.setStyleClass("icon");
					userBox.getChildren().add(userIcon);

					userBox.setId(user.getId() + "");

					Label userName = new Label(user.getName());
					userBox.getChildren().add(userName);

					Label userEmail = new Label("("+user.getEmail()+")");
					userBox.getChildren().add(userEmail);

					sharedContainer.getChildren().add(userBox);
				}
			} else {
				Label noSharedUser = new Label("Không có người dùng nào đã chia sẻ");
				sharedContainer.getChildren().add(noSharedUser);
			}
			centerContainer.getChildren().add(0, sharedContainer);
			shareTxt.setDisable(false);
		});

		getSharedUserTask.setOnFailed(event -> {
			System.out.println("Lỗi khi lấy danh sách người dùng đã chia sẻ");
			shareTxt.setDisable(false);
		});

		Thread thread2 = new Thread(getSharedUserTask);
		thread2.start();

		shareTxt.setOnKeyReleased(e -> {
			userContainer.getChildren().clear();
			Label userTitle1 = new Label("Danh sách tìm kiếm");
			userTitle1.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
			userContainer.getChildren().add(userTitle1);

			centerContainer.getChildren().remove(userContainer);

			String keyword = shareTxt.getText();
			if(keyword.length() > 0) {
				Task<List<User>> searchUserTask = new Task<List<User>>() {
					@Override
					protected List<User> call() throws Exception {
						ItemService itemService = new ItemService();
						List<User> userList = itemService.searchUnsharedUser(itemTypeId, itemId, keyword);
						return userList;
					}
				};

				searchUserTask.setOnSucceeded(event1 -> {
					List<User> userList = searchUserTask.getValue();
					if(userList != null) {
						for (User user : userList) {
							HBox userBox = new HBox();
							userBox.setSpacing(10);
							userBox.setAlignment(Pos.CENTER_LEFT);

							FontAwesomeIconView userIcon = new FontAwesomeIconView();
							userIcon.setGlyphName("USER");
							userIcon.setSize("20");
							userIcon.setStyleClass("icon");
							userBox.getChildren().add(userIcon);

							userBox.setId(user.getId() + "");

							Label userName = new Label(user.getName());
							userBox.getChildren().add(userName);

							Label userEmail = new Label("("+user.getEmail()+")");
							userBox.getChildren().add(userEmail);


							userBox.setOnMouseClicked(e1 -> {
								if(!userIds.contains(user.getId())) {
									userIds.add(user.getId());

									// copy userBox to newUserBox
									HBox newUserBox = new HBox();
									newUserBox.setSpacing(10);
									newUserBox.setAlignment(Pos.CENTER_LEFT);
									newUserBox.setId(user.getId() + "");

									Label newUserName = new Label(user.getName());
									newUserBox.getChildren().add(newUserName);

									Label newUserEmail = new Label("("+user.getEmail()+")");
									newUserBox.getChildren().add(newUserEmail);

									FontAwesomeIconView iconSelected = new FontAwesomeIconView();
									iconSelected.setGlyphName("CHECK");
									iconSelected.setSize("20");
									iconSelected.setStyleClass("icon");
									newUserBox.getChildren().add(iconSelected);

									userSelectedContainer.getChildren().add(newUserBox);
								}
							});

							userContainer.getChildren().add(userBox);
						}
					} else {
						Label noUser = new Label("Không có người dùng nào");
						userContainer.getChildren().add(noUser);
					}
					centerContainer.getChildren().add(1, userContainer);
				});

				searchUserTask.setOnFailed(event1 -> {
					System.out.println("Tìm kiếm thất bại");
				});

				Thread thread = new Thread(searchUserTask);
				thread.start();
			}
		});

		// if user click on the selected user, remove it from the list
		userSelectedContainer.setOnMouseClicked(e -> {
			Node target = (Node) e.getTarget();
			if(target instanceof HBox) {
				HBox userBox = (HBox) target;
				if(userBox.getId() != null) {
					int userId = Integer.parseInt(userBox.getId());
					userIds.remove((Integer) userId);
					userSelectedContainer.getChildren().remove(userBox);
				}
			}
		});

		centerContainer.getChildren().add(userSelectedContainer);
//		if(userIds.size() > 0) {
//			centerContainer.getChildren().add(userSelectedContainer);
//		} else {
//			centerContainer.getChildren().remove(userSelectedContainer);
//		}
		shareLayout.setCenter(scrollPane);

		Button shareBtn = new Button("Chia sẻ");
		shareBtn.setPrefWidth(100);
		shareBtn.setPrefHeight(30);
		shareBtn.setStyle("-fx-background-color: white");
		shareBtn.setStyle("-fx-border-color: gray");
		shareBtn.setStyle("-fx-border-width: 1px");

		shareBtn.setOnAction(e -> {
			String shareName = shareTxt.getText();
			shareStage.close();

			Task<Boolean> shareTask = new Task<Boolean>() {
				@Override
				protected Boolean call() throws Exception {
					ItemService itemService = new ItemService();
					int permissionId = permissionCbb.getValue().equals("Chỉ xem") ? 2 : 3;
					boolean rs = itemService.share(itemTypeId, itemId, permissionId, userId, userIds);
					return rs;
				}
			};

			shareTask.setOnSucceeded(event1 -> {
				boolean response = shareTask.getValue();
				System.out.println("Chia sẻ thành công");
			});

			shareTask.setOnFailed(event1 -> {
				System.out.println("Chia sẻ thất bại");
			});

			Thread thread = new Thread(shareTask);
			thread.start();
		});

		Button cancelBtn = new Button("Hủy");
		cancelBtn.setPrefWidth(100);
		cancelBtn.setPrefHeight(30);
		cancelBtn.setStyle("-fx-background-color: white");
		cancelBtn.setStyle("-fx-border-color: gray");
		cancelBtn.setStyle("-fx-border-width: 1px");

		cancelBtn.setOnAction(e -> {
			shareStage.close();
		});

		HBox btnContainer = new HBox();
		btnContainer.setSpacing(10);
		btnContainer.setAlignment(Pos.CENTER);
		btnContainer.getChildren().addAll(shareBtn, cancelBtn);

		shareLayout.setBottom(btnContainer);

		Scene scene = new Scene(shareLayout, 450, 300);
		shareStage.setScene(scene);
		shareStage.show();
	}
	@FXML
	public void shareClicked(ActionEvent event) {
		showSharePopup(1, currentFolderId);
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
				boolean rs = itemService.downloadFolder(selectedFolder.getAbsolutePath(), userId, currentFolderId);
				return rs;
			}
		};

		downloadFileTask.setOnSucceeded(e -> {
			boolean response = downloadFileTask.getValue();
			if(response) System.out.println("Download file thành công");
			else System.out.println("Download file thất bại");
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
				boolean rs = itemService.synchronizeFolder(userId , currentFolderId);
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
					boolean rs = itemService.createFolder(foldername, userId, currentFolderId);
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

	public void showAccessPopup(int itemTypeId, int itemId) {
		Stage accessStage = new Stage();
		accessStage.initModality(Modality.APPLICATION_MODAL);
		accessStage.setTitle("Quyền truy cập");

		accessStage.initStyle(StageStyle.UTILITY);

		BorderPane accessLayout = new BorderPane();
		accessLayout.setPadding(new Insets(10));


		Label title = new Label("Quyền truy cập");
		title.setStyle("-fx-font-size: 18;");
		accessLayout.setTop(title);

		ComboBox<String> permissionCbb = new ComboBox<>();
		permissionCbb.getItems().addAll("Riêng tư","Chỉ xem", "Chỉnh sửa");

//		Task<Integer> getPermissionTask = new Task<Integer>() {
//			@Override
//			protected Integer call() throws Exception {
//				PermissionService permissionService = new PermissionService();
//				return permissionService.getPermission(itemTypeId, itemId);
//			}
//		};
//
//		getPermissionTask.setOnSucceeded(e -> {
//			int permission = getPermissionTask.getValue();
//			if(permission == 1) permissionCbb.setValue("Riêng tư");
//			else if(permission == 2) permissionCbb.setValue("Chỉ xem");
//			else if(permission == 3) permissionCbb.setValue("Chỉnh sửa");
//		});
//
//		getPermissionTask.setOnFailed(e -> {
//			System.out.println("Lỗi khi lấy quyền truy cập");
//		});

		PermissionService permissionService = new PermissionService();
		int permission = permissionService.getPermission(itemTypeId, itemId);
		if(permission == 1) permissionCbb.setValue("Riêng tư");
		else if(permission == 2) permissionCbb.setValue("Chỉ xem");
		else if(permission == 3) permissionCbb.setValue("Chỉnh sửa");
		else permissionCbb.setValue("");

		permissionCbb.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1px; -fx-background-radius: 15px;");
		permissionCbb.setPrefWidth(200);
		permissionCbb.setPrefHeight(30);
		permissionCbb.setPadding(new Insets(5));

		accessLayout.setCenter(permissionCbb);

		Button accessBtn = new Button("Cập nhật");
		accessBtn.setPrefWidth(100);
		accessBtn.setPrefHeight(30);
		accessBtn.setStyle("-fx-background-color: white");
		accessBtn.setStyle("-fx-border-color: gray");
		accessBtn.setStyle("-fx-border-width: 1px");

		accessBtn.setOnAction(e -> {
			accessStage.close();

			int permissionId = 0;
			if(permissionCbb.getValue().equals("Riêng tư")) permissionId = 1;
			else if(permissionCbb.getValue().equals("Chỉ xem")) permissionId = 2;
			else if(permissionCbb.getValue().equals("Chỉnh sửa")) permissionId = 3;

			int finalPermissionId = permissionId;
			Task<Boolean> accessTask = new Task<Boolean>() {
				@Override
				protected Boolean call() throws Exception {
					PermissionService permissionService = new PermissionService();
					boolean rs = permissionService.updatePermission(itemTypeId, itemId, finalPermissionId);
					return rs;
				}
			};

			accessTask.setOnSucceeded(event1 -> {
				boolean response = accessTask.getValue();
				if(response) System.out.println("Cập nhật quyền truy cập thành công");
				else System.out.println("Cập nhật quyền truy cập thất bại");
			});

			accessTask.setOnFailed(event1 -> {
				System.out.println("Cập nhật quyền truy cập thất bại");
			});

			Thread thread = new Thread(accessTask);
			thread.start();
		});

		Button cancelBtn = new Button("Hủy");
		cancelBtn.setPrefWidth(100);
		cancelBtn.setPrefHeight(30);
		cancelBtn.setStyle("-fx-background-color: white");
		cancelBtn.setStyle("-fx-border-color: gray");
		cancelBtn.setStyle("-fx-border-width: 1px");

		cancelBtn.setOnAction(e -> accessStage.close());

		HBox footerLabel = new HBox();
		footerLabel.setSpacing(10);
		footerLabel.setPadding(new Insets(10));
		footerLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
		footerLabel.getChildren().addAll(accessBtn, cancelBtn);

		accessLayout.setBottom(footerLabel);

		Scene accessScene = new Scene(accessLayout, 300, 200);
		accessStage.setScene(accessScene);
		accessStage.showAndWait();
	}
	public void setFontLabel(int number) {
		for (int i = 0; i <= 5; ++i) {
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
				case 4:
					lbRecentOpenBtn.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.REGULAR, lbRecentOpenBtn.getFont().getSize()));
					break;
				case 5:
					lbTrashBtn.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.REGULAR, lbTrashBtn.getFont().getSize()));
			}
		}
	}
	public void generalPage(MouseEvent event) throws IOException {
		resetDatatable();
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
		List<models.File> itemList = itemService.getAllItem(userId,2, "");

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			final ObservableList<Object> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}

		PermissionService permissionService = new PermissionService();
		int permissionType = permissionService.checkPermission(userId, 1, currentFolderId);
		if(permissionType == 3) {
			createFolderBtn.setDisable(false);
			uploadFileBtn.setDisable(false);
			uploadFolderBtn.setDisable(false);
		}
		else {
			createFolderBtn.setDisable(true);
			uploadFileBtn.setDisable(true);
			uploadFolderBtn.setDisable(true);
		}
	}
	public void myFilePage(MouseEvent event) throws IOException {
		resetDatatable();
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
			final ObservableList<Object> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}

		createFolderBtn.setDisable(true);
		uploadFileBtn.setDisable(true);
		uploadFolderBtn.setDisable(true);
	}
	public void myShareFile(MouseEvent event) throws IOException {
		resetDatatable();
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
			final ObservableList<Object> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}

		createFolderBtn.setDisable(true);
		uploadFileBtn.setDisable(true);
		uploadFolderBtn.setDisable(true);
	}
	public void otherFileShare(MouseEvent event) throws IOException {
		resetDatatable();
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
			final ObservableList<Object> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}

		createFolderBtn.setDisable(true);
		uploadFileBtn.setDisable(true);
		uploadFolderBtn.setDisable(true);
	}
	public void search(ActionEvent event) throws IOException {
		resetDatatable();
		String txt = searchTxt.getText();
		System.out.println(txt);
		ItemService itemService = new ItemService();
		List<models.File> itemList = null;
		LoginSession loginSession = LoginService.getCurrentSession();
		int currentUserId = loginSession.getCurrentUserID();
		if (currentSideBarIndex == 0) {
			itemList = itemService.getAllItem(userId, currentFolderId, txt);
		} else if (currentSideBarIndex == 1) {
			if (currentFolderId == -1) {
				itemList = itemService.getAllItemPrivateOwnerId(currentUserId, txt);
			} else {
				itemList = itemService.getAllItem(userId, currentFolderId, txt);
			}
		} else if (currentSideBarIndex == 2) {
			if (currentFolderId == -2) {
				itemList = itemService.getAllOtherShareItem(currentUserId, txt);
			} else {
				itemList = itemService.getAllItem(userId, currentFolderId, txt);
			}
		} else if (currentSideBarIndex == 3) {
			if (currentFolderId == -3) {
				itemList = itemService.getAllSharedItem(currentUserId, txt);
			} else {
				itemList = itemService.getAllItem(userId, currentFolderId, txt);
			}
		} else if (currentSideBarIndex == 4){
			if (currentFolderId == -4) {
//				itemList = itemService.getAllRecentOpenedItem(userId, txt);
			} else {
				itemList = itemService.getAllItem(userId, currentFolderId, txt);
			}
		} else if (currentSideBarIndex == 5){
			if (currentFolderId == -5) {
				itemList = itemService.getAllDeletedItem(userId, txt);
			} else {
				itemList = itemService.getAllItem(userId, currentFolderId, txt);
			}
		}
		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			final ObservableList<Object> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}

		createFolderBtn.setDisable(true);
		uploadFileBtn.setDisable(true);
		uploadFolderBtn.setDisable(true);
	}
	private void fillDataBreadCrumb(int index) {
		resetDatatable();
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
			SortedList<Object> sortedData = new SortedList<>(items, (file1, file2) -> {
				if (((models.File)file1).getTypeId() == 1 && ((models.File)file2).getTypeId() != 1) {
					return -1;
				} else if (((models.File)file1).getTypeId() != 1 && ((models.File)file2).getTypeId() == 1) {
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


	public void resetDatatable() {
		dataTable.getColumns().clear();
		TableColumn<Object, String> nameColumn = new TableColumn<>("Tên");
		TableColumn<Object, String> ownerNameColumn = new TableColumn<>("Chủ sở hữu");
		TableColumn<Object, Date> dateModifiedColumn = new TableColumn<>("Đã sửa đổi");
		TableColumn<Object, String> lastModifiedByColumn = new TableColumn<>("Người sửa đổi");
		TableColumn<Object, String> sizeColumn = new TableColumn<>("Kích thước");

		dataTable.getColumns().addAll(nameColumn, ownerNameColumn, dateModifiedColumn, lastModifiedByColumn, sizeColumn);

		nameColumn.setCellValueFactory(column -> {
			return new SimpleStringProperty(((models.File)column.getValue()).getName() + (((models.File)column.getValue()).getTypeId() != 1 ? "." + ((models.File)column.getValue()).getTypesByTypeId().getName() : ""));
		});
		nameColumn.setCellFactory(column -> {
			return new TableCell<Object, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(empty || item == null || getTableRow() == null ||((models.File)getTableRow().getItem()) == null) {
						setText(null);
						setGraphic(null);
					}
					else {
						ImageView icon = new ImageView();
						icon.setFitHeight(20);
						icon.setFitWidth(20);
						if(((models.File)getTableRow().getItem()).getTypeId() == 1){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/folder.png").toString()));
						} else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().equals("txt")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/txt.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().matches("docx?|docm|dotx?|dotm")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/doc.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().equals("pdf")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/pdf.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().matches("mp4|mp3|avi|flv|wmv|mov|wav|wma|ogg|mkv")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/mp4.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().matches("png|svg|jpg|jpeg|gif|bmp")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/picture.png").toString()));
						}
						else {
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/unknown.png").toString()));
						}


						setGraphic(icon);
						setText(item);
					}
				}
			};
		});

		ownerNameColumn.setCellValueFactory(column -> {
			return new SimpleStringProperty(((models.File)column.getValue()).getUsersByOwnerId().getName() == null ? "" : ((models.File)column.getValue()).getUsersByOwnerId().getName());
		});
		dateModifiedColumn.setCellFactory(column -> {
			return new TableCell<Object, Date>() {
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
		dateModifiedColumn.setCellValueFactory(new PropertyValueFactory<Object, Date>("updatedAt"));
		lastModifiedByColumn.setCellValueFactory(column -> {
			return new SimpleStringProperty(((models.File)column.getValue()).getUsersByUpdatedBy() == null ? "" : ((models.File)column.getValue()).getUsersByUpdatedBy().getName());
		});
		sizeColumn.setCellValueFactory(column -> {
			int size = ((models.File)column.getValue()).getSize();
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
			TableRow<Object> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if(event.getButton() == MouseButton.PRIMARY && !row.isEmpty()){
					dataTable.getSelectionModel().select(row.getIndex());
					models.File file = ((models.File)row.getItem());
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
					showOptionsPopup(event, ((models.File)row.getItem()));
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

		dataTable.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/assets/css/tableview.css")).toExternalForm());
	}


	public void showTrashPage(MouseEvent mouseEvent) {
		dataTable.getColumns().clear();
		TableColumn<Object, String> nameColumn = new TableColumn<>("Tên");
		TableColumn<Object, Date> dateDeletedColumn = new TableColumn<>("Ngày xóa");
		TableColumn<Object, String> deletedByColumn = new TableColumn<>("Người xóa");
		TableColumn<Object, String> sizeColumn = new TableColumn<>("Kích thước");
		TableColumn<Object, String> addressColumn = new TableColumn<>("Vị trí ban đầu");

		dataTable.getColumns().addAll(nameColumn, dateDeletedColumn, deletedByColumn, sizeColumn, addressColumn);

		nameColumn.setCellValueFactory(column -> {
			return new SimpleStringProperty(((models.File)column.getValue()).getName() + (((models.File)column.getValue()).getTypeId() != 1 ? "." + ((models.File)column.getValue()).getTypesByTypeId().getName() : ""));
		});
		nameColumn.setCellFactory(column -> {
			return new TableCell<Object, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(empty || item == null || getTableRow() == null ||((models.File)getTableRow().getItem()) == null) {
						setText(null);
						setGraphic(null);
					}
					else {
						ImageView icon = new ImageView();
						icon.setFitHeight(20);
						icon.setFitWidth(20);
						if(((models.File)getTableRow().getItem()).getTypeId() == 1){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/folder.png").toString()));
						} else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().equals("txt")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/txt.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().matches("docx?|docm|dotx?|dotm")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/doc.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().equals("pdf")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/pdf.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().matches("mp4|mp3|avi|flv|wmv|mov|wav|wma|ogg|mkv")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/mp4.png").toString()));
						}
						else if (((models.File)getTableRow().getItem()).getTypesByTypeId().getName().matches("png|svg|jpg|jpeg|gif|bmp")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/picture.png").toString()));
						}
						else {
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/unknown.png").toString()));
						}


						setGraphic(icon);
						setText(item);
					}
				}
			};
		});

		dateDeletedColumn.setCellFactory(column -> {
			return new TableCell<Object, Date>() {
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
		dateDeletedColumn.setCellValueFactory(new PropertyValueFactory<Object, Date>("dateDeleted"));
		deletedByColumn.setCellValueFactory(column -> {
			return new SimpleStringProperty(((models.File)column.getValue()).getUsersByDeletedBy() == null ? "" : ((models.File)column.getValue()).getUsersByDeletedBy().getName());
		});
		sizeColumn.setCellValueFactory(column -> {
			int size = ((models.File)column.getValue()).getSize();
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
		addressColumn.setCellValueFactory(new PropertyValueFactory<Object, String>("finalpath"));

		dataTable.setRowFactory(dataTable -> {
			TableRow<Object> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if(event.getButton() == MouseButton.PRIMARY && !row.isEmpty()){
					dataTable.getSelectionModel().select(row.getIndex());
					models.File file = ((models.File)row.getItem());
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
					showDeleteOptionsPopup(event, ((models.File)row.getItem()));
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

		dataTable.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/assets/css/tableview.css")).toExternalForm());

		lbTrashBtn.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, lbTrashBtn.getFont().getSize()));
		setFontLabel(5);
		searchTxt.setText("");
		currentSideBarIndex = 5;
		currentFolderId = -5;
		breadcrumbList.clear();
		HBox breadcrumb = createBreadcrumb(-5, "Thùng rác");
		breadcrumbList.add(breadcrumb);
		// Thêm các HBox breadcrumb vào container
		path.getChildren().setAll(breadcrumbList);

		ItemService itemService = new ItemService();
		List<models.File> itemList = itemService.getAllDeletedItem(userId, "");

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			final ObservableList<Object> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}

		createFolderBtn.setDisable(true);
		uploadFileBtn.setDisable(true);
		uploadFolderBtn.setDisable(true);
	}

	private void fillDeletedData() {
		ItemService itemService = new ItemService();
		List<models.File> itemList = itemService.getAllDeletedItem(userId, "");

		if(itemList == null) {
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			items.clear();
			items.addAll(itemList);

			// Tạo SortedList với Comparator để xác định thứ tự của folders và files
			SortedList<Object> sortedData = new SortedList<>(items, (file1, file2) -> {
				if (((models.File)file1).getTypeId() == 1 && ((models.File)file2).getTypeId() != 1) {
					return -1;
				} else if (((models.File)file1).getTypeId() != 1 && ((models.File)file2).getTypeId() == 1) {
					return 1;
				}
				return 0;

			});

			dataTable.setItems(sortedData);
			sortedData.comparatorProperty().bind(dataTable.comparatorProperty());
		}

		createFolderBtn.setDisable(true);
		uploadFileBtn.setDisable(true);
		uploadFolderBtn.setDisable(true);
	}

	private void showDeleteOptionsPopup(MouseEvent mouseEvent, models.File selectedItem) {
		Popup popup = new Popup();
		popup.setAutoHide(true);
		popup.setAutoFix(true);
		popup.setHideOnEscape(true);

		FontAwesomeIconView deleteIcon = new FontAwesomeIconView();
		deleteIcon.setGlyphName("TRASH");
		deleteIcon.setSize("20");
		deleteIcon.setStyleClass("icon");
		Button deleteBtn = new Button("Xóa vĩnh viễn", deleteIcon);

		FontAwesomeIconView restoreIcon = new FontAwesomeIconView();
		restoreIcon.setGlyphName("REFRESH");
		restoreIcon.setSize("20");
		restoreIcon.setStyleClass("icon");
		Button restoreBtn = new Button("Khôi phục", restoreIcon);

		deleteBtn.setOnAction(event -> {
			// Delete file
			int itemTypeId = selectedItem.getTypeId();
			int itemId = selectedItem.getId();

			sendDeletePermanentlyRequest(itemTypeId, itemId);

			popup.hide();
		});

		restoreBtn.setOnAction(event -> {
			// Restore file
			int itemTypeId = selectedItem.getTypeId();
			int itemId = selectedItem.getId();
			sendRestoreRequest(itemTypeId, itemId);

			popup.hide();
		});

		VBox options = new VBox();
		options.setPrefWidth(150);
		options.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-radius: 15px; -fx-border-width: 1px; -fx-background-radius: 15px;");

		options.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

		for (Button button : Arrays.asList(deleteBtn, restoreBtn)) {
			if (button != null) {
				button.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				button.setPadding(new Insets(5, 5, 5, 15));
				button.setPrefWidth(150);

				if (button == deleteBtn) {
					button.setStyle("-fx-background-color: transparent; -fx-background-radius: 15px 15px 0px 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
					button.setOnMouseEntered(event -> {
						button.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 15px 15px 0px 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
					button.setOnMouseExited(event -> {
						button.setStyle("-fx-background-color: transparent; -fx-background-radius: 15px 15px 0px 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
				} else if(button == restoreBtn) {
					button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px 0px 15px 15px; -fx-background-insets: 0px; -fx-border-width: 0;");
					button.setOnMouseEntered(event -> {
						button.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 0px 0px 15px 15px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
					button.setOnMouseExited(event -> {
						button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px 0px 15px 15px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
				}
			}
		}

		options.getChildren().addAll(deleteBtn, restoreBtn);
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

	public void showRecentOpenPage(MouseEvent mouseEvent) {
		dataTable.getColumns().clear();
		TableColumn<Object, String> nameColumn = new TableColumn<>("Tên");
		TableColumn<Object, Date> dateOpenedColumn = new TableColumn<>("Ngày mở");
		TableColumn<Object, String> ownerColumn = new TableColumn<>("Chủ sở hữu");
		TableColumn<Object, String> addressColumn = new TableColumn<>("Vị trí");

		dataTable.getColumns().addAll(nameColumn, dateOpenedColumn, ownerColumn, addressColumn);

		nameColumn.setCellValueFactory(column -> {
			return new SimpleStringProperty(((RecentFile)column.getValue()).getFilesByFileId().getName() + (((RecentFile)column.getValue()).getFilesByFileId().getTypeId() != 1 ? "." + ((RecentFile)column.getValue()).getFilesByFileId().getTypesByTypeId().getName() : ""));
		});
		nameColumn.setCellFactory(column -> {
			return new TableCell<Object, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(empty || item == null || getTableRow() == null ||((RecentFile)getTableRow().getItem()) == null) {
						setText(null);
						setGraphic(null);
					}
					else {
						ImageView icon = new ImageView();
						icon.setFitHeight(20);
						icon.setFitWidth(20);
						if (((RecentFile)getTableRow().getItem()).getFilesByFileId().getTypesByTypeId().getName().equals("txt")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/txt.png").toString()));
						}
						else if (((RecentFile)getTableRow().getItem()).getFilesByFileId().getTypesByTypeId().getName().matches("docx?|docm|dotx?|dotm")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/doc.png").toString()));
						}
						else if (((RecentFile)getTableRow().getItem()).getFilesByFileId().getTypesByTypeId().getName().equals("pdf")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/pdf.png").toString()));
						}
						else if (((RecentFile)getTableRow().getItem()).getFilesByFileId().getTypesByTypeId().getName().matches("mp4|mp3|avi|flv|wmv|mov|wav|wma|ogg|mkv")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/mp4.png").toString()));
						}
						else if (((RecentFile)getTableRow().getItem()).getFilesByFileId().getTypesByTypeId().getName().matches("png|svg|jpg|jpeg|gif|bmp")){
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/picture.png").toString()));
						}
						else {
							icon.setImage(new javafx.scene.image.Image(getClass().getResource("/assets/images/unknown.png").toString()));
						}


						setGraphic(icon);
						setText(item);
					}
				}
			};
		});

		dateOpenedColumn.setCellFactory(column -> {
			return new TableCell<Object, Date>() {
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
		dateOpenedColumn.setCellValueFactory(new PropertyValueFactory<Object, Date>("openedAt"));
		ownerColumn.setCellValueFactory(column -> {
			return new SimpleStringProperty(((RecentFile)column.getValue()).getFilesByFileId().getUsersByOwnerId() == null ? "" : ((RecentFile)column.getValue()).getFilesByFileId().getUsersByOwnerId().getName());
		});
		addressColumn.setCellValueFactory(column -> {
			return new SimpleStringProperty(((RecentFile)column.getValue()).getFilesByFileId() == null ? "" : ((RecentFile)column.getValue()).getFilesByFileId().getFinalpath());
		});

		dataTable.setRowFactory(dataTable -> {
			TableRow<Object> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if(event.getButton() == MouseButton.PRIMARY && !row.isEmpty()){
					dataTable.getSelectionModel().select(row.getIndex());
					RecentFile file = ((RecentFile)row.getItem());
					if(file.getFilesByFileId().getTypeId() == 1){
						currentFolderId = file.getId();
						fillData();
						// Tạo HBox breadcrumb mới
						HBox _breadcrumb = createBreadcrumb(file.getId(), file.getFilesByFileId().getName());
						breadcrumbList.add(_breadcrumb);
						// Thêm các HBox breadcrumb vào container
						path.getChildren().setAll(breadcrumbList);
					}
					else {
						// Open file
					}
				} else if(event.getButton() == MouseButton.SECONDARY && !row.isEmpty()){
					dataTable.getSelectionModel().select(row.getIndex());
//					showDeleteOptionsPopup(event, ((models.File)row.getItem()));
					showRecentOptionsPopup(event, ((RecentFile)row.getItem()));
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

		dataTable.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/assets/css/tableview.css")).toExternalForm());

		lbRecentOpenBtn.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, lbTrashBtn.getFont().getSize()));
		setFontLabel(4);
		searchTxt.setText("");
		currentSideBarIndex = 4;
		currentFolderId = -4;
		breadcrumbList.clear();
		HBox breadcrumb = createBreadcrumb(-4, "Gần đây");
		breadcrumbList.add(breadcrumb);
		// Thêm các HBox breadcrumb vào container
		path.getChildren().setAll(breadcrumbList);

		ItemService itemService = new ItemService();
		List<RecentFile> itemList = itemService.getAllRecentOpenedItem(userId, "");

		System.out.println("itemList: " + itemList);

		if(itemList == null) {
			System.out.println("null");
			dataTable.setPlaceholder(new Label("Không có dữ liệu"));
		}
		else {
			final ObservableList<Object> items = FXCollections.observableArrayList(itemList);
			dataTable.setItems(items);
			System.out.println("not null");
		}

		createFolderBtn.setDisable(true);
		uploadFileBtn.setDisable(true);
		uploadFolderBtn.setDisable(true);
	}

	private void showRecentOptionsPopup(MouseEvent mouseEvent, RecentFile selectedItem) {
		Popup popup = new Popup();
		popup.setAutoHide(true);
		popup.setAutoFix(true);
		popup.setHideOnEscape(true);

		FontAwesomeIconView openIcon = new FontAwesomeIconView();
		openIcon.setGlyphName("FOLDER");
		openIcon.setSize("20");
		openIcon.setStyleClass("icon");
		Button openBtn = new Button("Mở", openIcon);


		FontAwesomeIconView openLocationIcon = new FontAwesomeIconView();
		openLocationIcon.setGlyphName("SHARE_SQUARE");
		openLocationIcon.setSize("20");
		openLocationIcon.setStyleClass("icon");
		Button openLocationBtn = new Button("Mở vị trí", openLocationIcon);


		openBtn.setOnAction(event -> {
			// Open file
			int itemTypeId = selectedItem.getFilesByFileId().getTypeId();
			int itemId = selectedItem.getFilesByFileId().getId();

			Task<String> openTask = new Task<String>() {
				@Override
				protected String call() throws Exception {
					ItemService itemService = new ItemService();
					if(itemTypeId == 1) {
						return itemService.openFolder(userId, itemId);
					} else {
						return itemService.openFile(userId, itemId);
					}
				}
			};

			openTask.setOnSucceeded(e -> {
				String path = openTask.getValue();
				if(path!=null && !path.isEmpty()) {
					// Open file
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(new File(path));
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}

					System.out.println("Mở thành công");
				}
				else System.out.println("Mở thất bại");
			});

			openTask.setOnFailed(e -> {
				System.out.println("Mở thất bại");
			});

			Thread thread = new Thread(openTask);
			thread.start();

			popup.hide();
		});

		openLocationBtn.setOnAction(event -> {
//			// Open location of file

			resetDatatable();
			lbGeneral.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, lbGeneral.getFont().getSize()));
			setFontLabel(0);
			searchTxt.setText("");
			currentSideBarIndex = 0;
			breadcrumbList.clear();

			Folder folder = selectedItem.getFilesByFileId().getFoldersByFolderId();
			while (folder.getId() != 2) {
				HBox breadcrumb = createBreadcrumb(folder.getId(), folder.getFolderName());
				breadcrumbList.add(0, breadcrumb);
				folder = folder.getFoldersByParentId();
			}
			HBox breadcrumb = createBreadcrumb(2, "Chung");
			breadcrumbList.add(0, breadcrumb);
			// Thêm các HBox breadcrumb vào container
			path.getChildren().setAll(breadcrumbList);

			currentFolderId = selectedItem.getFilesByFileId() == null ? 2 : selectedItem.getFilesByFileId().getFolderId();
			fillData();

			popup.hide();
		});

		VBox options = new VBox();
		options.setPrefWidth(150);
		options.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-radius: 15px; -fx-border-width: 1px; -fx-background-radius: 15px;");

		options.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
		for (Button button : Arrays.asList(openBtn, openLocationBtn)) {
			if (button != null) {
				button.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				button.setPadding(new Insets(5, 5, 5, 15));
				button.setPrefWidth(150);

				if (button == openBtn) {
					button.setStyle("-fx-background-color: transparent; -fx-background-radius: 15px 15px 0px 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
					button.setOnMouseEntered(event -> {
						button.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 15px 15px 0px 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
					button.setOnMouseExited(event -> {
						button.setStyle("-fx-background-color: transparent; -fx-background-radius: 15px 15px 0px 0px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
				} else if(button == openLocationBtn) {
					button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px 0px 15px 15px; -fx-background-insets: 0px; -fx-border-width: 0;");
					button.setOnMouseEntered(event -> {
						button.setStyle("-fx-background-color: #f1f1f1; -fx-background-radius: 0px 0px 15px 15px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
					button.setOnMouseExited(event -> {
						button.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px 0px 15px 15px; -fx-background-insets: 0px; -fx-border-width: 0;");
					});
				}
			}
		}

		options.getChildren().addAll(openBtn, openLocationBtn);

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
}
