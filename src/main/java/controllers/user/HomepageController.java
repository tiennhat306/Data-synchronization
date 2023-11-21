package controllers.user;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Type;
import models.User;
import services.client.user.ItemService;

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
	private int fileIdToMove = -1;
	private int targetFolderId = -1;
	private int fileTypeToMove = -1;
	
	private int fileIdToCopy = -1;
	private int targetFolderCopyId = -1;
	private int fileTypeToCopy = -1;
	private ObservableList<models.File> items = FXCollections.observableArrayList();

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

		dataTable.setRowFactory(dataTable -> {
			TableRow<models.File> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if(event.getButton() == MouseButton.PRIMARY && !row.isEmpty()){
					dataTable.getSelectionModel().select(row.getIndex());
					models.File file = row.getItem();
					if(file.getTypeId() == 1){
						currentFolderId = file.getId();
						fillData();
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
		
		FontAwesomeIconView pasteIcon = new FontAwesomeIconView();
		pasteIcon.setGlyphName("CLIPBOARD"); // Fix the variable name to pasteIcon
		pasteIcon.setSize("20");
		pasteIcon.setStyleClass("icon");
		Button pasteBtn = new Button("Dán", pasteIcon);

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
			//downloadFileClicked(selectedItem.getId(), selectedItem.getName(), selectedItem.getTypeId());
            downloadFolderClicked(selectedItem.getId(), selectedItem.getName());
			popup.hide();
		});

		deleteBtn.setOnAction(event -> {
			// Delete file
			deleteFile(selectedItem.getId());
			deleteFolder(selectedItem.getId());
			popup.hide();
		});

		renameBtn.setOnAction(event -> {
		    // Rename file or folder
		    popup.hide();

		    // Get the new name from the user
		    TextInputDialog dialog = new TextInputDialog();
		    dialog.setTitle("Rename Item");
		    dialog.setHeaderText(null);
		    dialog.setContentText("Enter the new name:");

		    // Show the dialog and wait for the user's input
		    dialog.showAndWait().ifPresent(newName -> {
		        // Ensure the new name is not empty
		        if (!newName.trim().isEmpty()) {
		            if (selectedItem.toString().contains("typeId=1")) {
		                // Call the renameFile method with the selected item's ID and the new name
		            	renameFolder(selectedItem.getId(), newName);
		                
		            } else {
		                // Call the renameFolder method with the selected item's ID and the new name
		            	renameFile(selectedItem.getId(), newName);
		            } 
		        } else {
		            // Handle the case where the user entered an empty name
		            System.out.println("Invalid name");
		        }
		    });
		});

		moveBtn.setOnAction(event -> {
			// Move file
			popup.hide();
			fileIdToMove = selectedItem.getId();	
			fileTypeToMove = selectedItem.getTypeId();
			System.out.println(fileTypeToMove);
		});

		copyBtn.setOnAction(event -> {
			// Copy file
			fileIdToCopy = selectedItem.getId();	
			fileTypeToCopy = selectedItem.getTypeId();
			System.out.println(fileTypeToCopy);
			popup.hide();
		});
		
		pasteBtn.setOnAction(event -> {
			// Copy file
			popup.hide();
			if(fileTypeToMove == 1) {
				targetFolderId = selectedItem.getId();
			    // Check if fileIdToMove and targetFolderId are valid before attempting to move
			    if (fileIdToMove != -1 && targetFolderId != -1) {
			        moveFolder(fileIdToMove, targetFolderId);
			        // Reset the stored values after the move operation
			        fileIdToMove = -1;
			        targetFolderId = -1;
			    } else {
			        System.out.println("Invalid fileId or folderId");
			    }
			} else {
				targetFolderId = selectedItem.getId();
			    // Check if fileIdToMove and targetFolderId are valid before attempting to move
			    if (fileIdToMove != -1 && targetFolderId != -1) {
			        moveFile(fileIdToMove, targetFolderId);
			        // Reset the stored values after the move operation
			        fileIdToMove = -1;
			        targetFolderId = -1;
			    } else {
			        System.out.println("Invalid fileId or folderId");
			    }
			}
			
			if(fileTypeToCopy == 1) {
				targetFolderCopyId = selectedItem.getId();
			    // Check if fileIdToMove and targetFolderId are valid before attempting to move
			    if (fileIdToCopy != -1 && targetFolderCopyId != -1) {
			        copyFolder(fileIdToCopy, targetFolderCopyId);
			        // Reset the stored values after the move operation
			        fileIdToCopy = -1;
			        targetFolderCopyId = -1;
			    } else {
			        System.out.println("Invalid fileId or folderId");
			    }
			} else {
				targetFolderCopyId = selectedItem.getId();
			    // Check if fileIdToMove and targetFolderId are valid before attempting to move
			    if (fileIdToCopy != -1 && targetFolderCopyId != -1) {
			        copyFile(fileIdToCopy, targetFolderCopyId);
			        // Reset the stored values after the move operation
			        fileIdToCopy = -1;
			        targetFolderCopyId = -1;
			    } else {
			        System.out.println("Invalid fileId or folderId");
			    }
			}
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
		options.setStyle("-fx-background-color: white");
		options.setStyle("-fx-border-color: gray");
		options.setStyle("-fx-border-width: 1px");

		options.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
		options.setStyle("-fx-background-radius: 5px");
		for (Button button : Arrays.asList(openBtn, downloadBtn, deleteBtn, renameBtn, moveBtn, copyBtn, pasteBtn, shareBtn, synchronizeBtn)) {
			if (button != null) {
				button.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
				button.setPadding(new Insets(5, 5, 5, 15));
				button.setPrefWidth(150);
				button.setStyle("-fx-background-color: white");
				button.setStyle("-fx-border-color: gray");
				button.setStyle("-fx-border-width: 0 0 0 0");

			
			}
		}

		options.getChildren().addAll(openBtn, downloadBtn, deleteBtn, renameBtn, moveBtn, copyBtn, pasteBtn, shareBtn, synchronizeBtn);
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
		List<models.File> itemList = itemService.getAllItem(currentFolderId);

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
		// Create a FileChooser
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a file to upload");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

		if (selectedFiles != null) {
			for(File file : selectedFiles){
				// Get the selected file's name
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
					if(response) fillData();
					else System.out.println("Upload file thành công");
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
	
	public void deleteFile(int fileID) {
	    Task<Boolean> deleteFileTask = new Task<Boolean>() {
	        @Override
	        protected Boolean call() throws Exception {
	            try {
	                // Initialize your service outside the call method if it's not a short-lived service
	                ItemService itemService = new ItemService();

	                // Perform the background operation
	                return itemService.deleteFileIfExist(fileID);
	            } catch (Exception e) {
	                // Handle exceptions gracefully, you might want to log them or show an error dialog
	                e.printStackTrace();
	                return false;
	            }
	        }
	    };

	    deleteFileTask.setOnSucceeded(e -> {
	        boolean response = deleteFileTask.getValue();
			if(response) fillData();
			else System.out.println("Xoá file thành công");
	    });

	    deleteFileTask.setOnFailed(e -> {
	    	System.out.println("Xoá file thất bại");
	    });

	    // Start the task in a new thread
	    Thread thread = new Thread(deleteFileTask);
	    thread.start();
	}
	
	public void moveFile(int fileID, int folderID) {
	    Task<Boolean> moveFileTask = new Task<Boolean>() {
	        @Override
	        protected Boolean call() throws Exception {
	            try {
	                // Initialize your service outside the call method if it's not a short-lived service
	                ItemService itemService = new ItemService();

	                // Perform the background operation
	                return itemService.moveFile(fileID, folderID);
	            } catch (Exception e) {
	                // Handle exceptions gracefully, you might want to log them or show an error dialog
	                e.printStackTrace();
	                return false;
	            }
	        }
	    };

	    moveFileTask.setOnSucceeded(e -> {
	        boolean response = moveFileTask.getValue();
			if(response) fillData();
			else System.out.println("Di chuyển file thành công");
	    });

	    moveFileTask.setOnFailed(e -> {
	    	System.out.println("Di chuyển file thất bại");
	    });

	    // Start the task in a new thread
	    Thread thread = new Thread(moveFileTask);
	    thread.start();
	}
	
	public void moveFolder(int folderID, int parentID) {
	    Task<Boolean> moveFolderTask = new Task<Boolean>() {
	        @Override
	        protected Boolean call() throws Exception {
	            try {
	                // Initialize your service outside the call method if it's not a short-lived service
	                ItemService itemService = new ItemService();

	                // Perform the background operation
	                return itemService.moveFolder(folderID, parentID);
	            } catch (Exception e) {
	                // Handle exceptions gracefully, you might want to log them or show an error dialog
	                e.printStackTrace();
	                return false;
	            }
	        }
	    };

	    moveFolderTask.setOnSucceeded(e -> {
	        boolean response = moveFolderTask.getValue();
			if(response) fillData();
			else System.out.println("Di chuyển folder thành công");
	    });

	    moveFolderTask.setOnFailed(e -> {
	    	System.out.println("Di chuyển folder thất bại");
	    });

	    // Start the task in a new thread
	    Thread thread = new Thread(moveFolderTask);
	    thread.start();
	}

	public void deleteFolder(int folderID) {
	    Task<Boolean> deleteFolderTask = new Task<Boolean>() {
	        @Override
	        protected Boolean call() throws Exception {
	            try {
	                // Initialize your service outside the call method if it's not a short-lived service
	                ItemService itemService = new ItemService();

	                // Perform the background operation
	                return itemService.deleteFolderIfExist(folderID);
	            } catch (Exception e) {
	                // Handle exceptions gracefully, you might want to log them or show an error dialog
	                e.printStackTrace();
	                return false;
	            }
	        }
	    };

	    deleteFolderTask.setOnSucceeded(e -> {
	        boolean response = deleteFolderTask.getValue();
			if(response) fillData();
			else System.out.println("Xoá file thành công");
	    });

	    deleteFolderTask.setOnFailed(e -> {
	    	System.out.println("Xoá file thất bại");
	    });

	    // Start the task in a new thread
	    Thread thread = new Thread(deleteFolderTask);
	    thread.start();
	}
	
	public void renameFile(int fileID, String fileName) {
	    Task<Boolean> renameFileTask = new Task<Boolean>() {
	        @Override
	        protected Boolean call() throws Exception {
	            try {
	                // Initialize your service outside the call method if it's not a short-lived service
	                ItemService itemService = new ItemService();

	                // Perform the background operation
	                return itemService.renameFile(fileID, fileName);
	            } catch (Exception e) {
	                // Handle exceptions gracefully, you might want to log them or show an error dialog
	                e.printStackTrace();
	                return false;
	            }
	        }
	    };

	    renameFileTask.setOnSucceeded(e -> {
	        boolean response = renameFileTask.getValue();
			if(response) fillData();
			else System.out.println("Đổi tên file thành công");
	    });

	    renameFileTask.setOnFailed(e -> {
	    	System.out.println("Đổi tên file thất bại");
	    });

	    // Start the task in a new thread
	    Thread thread = new Thread(renameFileTask);
	    thread.start();
	}
	
	public void copyFile(int fileID, int folderID) {
	    Task<Boolean> copyFileTask = new Task<Boolean>() {
	        @Override
	        protected Boolean call() throws Exception {
	            try {
	                // Initialize your service outside the call method if it's not a short-lived service
	                ItemService itemService = new ItemService();

	                // Perform the background operation
	                return itemService.copyFile(fileID, folderID);
	            } catch (Exception e) {
	                // Handle exceptions gracefully, you might want to log them or show an error dialog
	                e.printStackTrace();
	                return false;
	            }
	        }
	    };

	    copyFileTask.setOnSucceeded(e -> {
	        boolean response = copyFileTask.getValue();
			if(response) fillData();
			else System.out.println("Sao chép file thành công");
	    });

	    copyFileTask.setOnFailed(e -> {
	    	System.out.println("Sao chép file thất bại");
	    });

	    // Start the task in a new thread
	    Thread thread = new Thread(copyFileTask);
	    thread.start();
	}
	
	public void copyFolder(int folderID, int parentID) {
	    Task<Boolean> copyFolderTask = new Task<Boolean>() {
	        @Override
	        protected Boolean call() throws Exception {
	            try {
	                // Initialize your service outside the call method if it's not a short-lived service
	                ItemService itemService = new ItemService();

	                // Perform the background operation
	                return itemService.copyFolder(folderID, parentID);
	            } catch (Exception e) {
	                // Handle exceptions gracefully, you might want to log them or show an error dialog
	                e.printStackTrace();
	                return false;
	            }
	        }
	    };

	    copyFolderTask.setOnSucceeded(e -> {
	        boolean response = copyFolderTask.getValue();
			if(response) fillData();
			else System.out.println("Sao chép folder thành công");
	    });

	    copyFolderTask.setOnFailed(e -> {
	    	System.out.println("Sao chép folder thất bại");
	    });

	    // Start the task in a new thread
	    Thread thread = new Thread(copyFolderTask);
	    thread.start();
	}
	
	public void renameFolder(int folderID, String folderName) {
	    Task<Boolean> renameFolderTask = new Task<Boolean>() {
	        @Override
	        protected Boolean call() throws Exception {
	            try {
	                // Initialize your service outside the call method if it's not a short-lived service
	                ItemService itemService = new ItemService();

	                // Perform the background operation
	                return itemService.renameFolder(folderID, folderName);
	            } catch (Exception e) {
	                // Handle exceptions gracefully, you might want to log them or show an error dialog
	                e.printStackTrace();
	                return false;
	            }
	        }
	    };

	    renameFolderTask.setOnSucceeded(e -> {
	        boolean response = renameFolderTask.getValue();
			if(response) fillData();
			else System.out.println("Đổi tên folder thành công");
	    });

	    renameFolderTask.setOnFailed(e -> {
	    	System.out.println("Đổi tên folder thất bại");
	    });

	    // Start the task in a new thread
	    Thread thread = new Thread(renameFolderTask);
	    thread.start();
	}
	
	@FXML
	public void handleUploadFolderButtonAction() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a folder to upload");
		
		// Show the folder dialog and get the selected folder
		File selectedFolder = directoryChooser.showDialog(null);

		if (selectedFolder != null && selectedFolder.isDirectory()) {
			// Get the selected folder's name
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
	
	public void downloadFolderClicked(int folderID, String folderName) {
		// Create a FileChooser to choose the path to save file
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a folder to save file");
//		directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		File selectedFolder = directoryChooser.showDialog(null);

		Task<Boolean> downloadFileTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				ItemService itemService = new ItemService();
				boolean rs = itemService.downloadFolderNew(selectedFolder.getAbsolutePath(), folderID, folderName);
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
	
	public void downloadFileClicked(int fileID, String fileName, int TypeID) {
		// Create a FileChooser to choose the path to save file
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose a folder to save file");
//		directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		File selectedFolder = directoryChooser.showDialog(null);

		Task<Boolean> downloadFileTask = new Task<Boolean>() {
			@Override
			protected Boolean call() throws Exception {
				ItemService itemService = new ItemService();
				boolean rs = itemService.downloadFile(selectedFolder.getAbsolutePath(), fileID, fileName, TypeID);
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
}