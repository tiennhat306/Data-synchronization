package services.user;

import models.File;
import models.Folder;

import org.hibernate.Session;
import org.hibernate.Transaction;

import DTO.Item;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileService {
    private final Session session;
    public FileService() {
        this.session = null;
    }
    public FileService(Session session) {
        this.session = session;
    }
    public List<File> getAllFile(int folderId) {
        try {
            //print session
            System.out.println("session: " + session);

            return session.createQuery("select f from File f where f.folderId = :folderId", File.class)
                    .setParameter("folderId", folderId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Item> getAllItem(int folderId) {
		try {
			List<Item> itemList = new ArrayList<>();
			List<Folder> folderList = session
					.createQuery("select fd from Folder fd where fd.parentId = :folderId", Folder.class)
					.setParameter("folderId", folderId).list();
			System.out.println("folderList: " + folderList);
			if (folderList != null) {
				for (Folder folder : folderList) {
					Item folderItem = new Item();
					folderItem.setId(folder.getId());
					folderItem.setTypeId(0);
					folderItem.setName(folder.getFolderName());
					folderItem.setOwnerName(folder.getUsersByOwnerId().getName());

					FolderService folderService = new FolderService(session);
					Pair<Date, Integer> updatedFolderInfo = folderService.getLastModifiedInfo(folder.getId());
					folderItem.setDateModified(updatedFolderInfo.getKey());

					UserService userService = new UserService(session);
					folderItem.setLastModifiedBy(updatedFolderInfo.getValue() == null ? ""
							: userService.getUserById(updatedFolderInfo.getValue()).getName());

					int countItem = folderService.getNumberItemOfFolder(folder.getId());
					folderItem.setSize(countItem + " mục");

					System.out.println("folderItem: " + folderItem);
					itemList.add(folderItem);
				}
			}

			List<File> fileList = session.createQuery("select f from File f where f.folderId = :folderId", File.class)
					.setParameter("folderId", folderId).list();
			System.out.println("fileList: " + fileList);
			if (fileList != null) {
				for (File file : fileList) {
					Item fileItem = new Item();
					fileItem.setId(file.getId());
					fileItem.setTypeId(file.getTypeId());
					fileItem.setName(file.getName() + "." + file.getTypesByTypeId().getName());
					fileItem.setOwnerName(file.getUsersByOwnerId().getName());
					fileItem.setDateModified(file.getUpdatedAt());
					fileItem.setLastModifiedBy(file.getUpdatedBy() == null ? "" : file.getUsersByUpdatedBy().getName());

					String sizeString = "";
					long size = file.getSize();
					if (size < 1024) {
						sizeString = size + " B";
					} else if (size < 1024 * 1024) {
						sizeString = size / 1024 + " KB";
					} else if (size < 1024 * 1024 * 1024) {
						sizeString = size / 1024 / 1024 + " MB";
					} else {
						sizeString = size / 1024 / 1024 / 1024 + " GB";
					}
					fileItem.setSize(sizeString);
					System.out.println("fileItem: " + fileItem);
					itemList.add(fileItem);
				}
			}

			return itemList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

    public File getFileById(int id) {
        try {
            return session.find(File.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean addFile(File file) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(file);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateFile(File file) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(file);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteFile(int id) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            File file = session.find(File.class, id);
            session.remove(file);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<File> getFilesByOwnerId(int userId) {
        try {
            return session.createQuery("select f from File f where f.ownerId = :userId", File.class)
                    .setParameter("userId", userId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<File> getFilesByFolderId(int folderId) {
        try {
            return session.createQuery("select f from File f where f.folderId = :folderId", File.class)
                    .setParameter("folderId", folderId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<File> getFilesByType(int type_id) {
        try {
            return session.createQuery("select f from File f where f.typeId = :type_id", File.class)
                    .setParameter("type_id", type_id)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
