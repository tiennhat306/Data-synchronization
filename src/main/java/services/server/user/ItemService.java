package services.server.user;

import DTO.Item;
import javafx.util.Pair;
import models.File;
import models.Folder;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemService {
    public ItemService() {
    }

    public List<Item> getAllItem(int folderId){
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Item> itemList = new ArrayList<>();
            List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :folderId", Folder.class)
                    .setParameter("folderId", folderId)
                    .list();
            System.out.println("folderList: " + folderList);
            if(folderList != null) {
                for (Folder folder : folderList) {
                    Item folderItem = new Item();
                    folderItem.setId(folder.getId());
                    folderItem.setTypeId(0);
                    folderItem.setName(folder.getFolderName());
                    folderItem.setOwnerName(folder.getUsersByOwnerId().getName());

                    FolderService folderService = new FolderService();
                    Pair<Date, Integer> updatedFolderInfo = folderService.getLastModifiedInfo(folder.getId());
                    folderItem.setDateModified(updatedFolderInfo.getKey());

                    UserService userService = new UserService();
                    folderItem.setLastModifiedBy(updatedFolderInfo.getValue() == null ? "" : userService.getUserById(updatedFolderInfo.getValue()).getName());


                    int countItem = folderService.getNumberItemOfFolder(folder.getId());
                    folderItem.setSize(countItem + " mục");

                    System.out.println("folderItem: " + folderItem);
                    itemList.add(folderItem);
                }
            }

            List<File> fileList = session.createQuery("select f from File f where f.folderId = :folderId", File.class)
                    .setParameter("folderId", folderId)
                    .list();
            System.out.println("fileList: " + fileList);
            if(fileList != null) {
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

}
