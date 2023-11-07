package services.server.user;

import javafx.util.Pair;
import models.File;
import models.Folder;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemService {
    public ItemService() {
    }

    public List<File> getAllItem(int folderId){
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<File> itemList = new ArrayList<>();
            List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :folderId", Folder.class)
                    .setParameter("folderId", folderId)
                    .list();
            System.out.println("folderList: " + folderList);
            if(folderList != null) {
                for (Folder folder : folderList) {
                    File folderToFile = new File();
                    folderToFile.setId(folder.getId());
                    folderToFile.setName(folder.getFolderName());
                    folderToFile.setTypeId(1);
                    folderToFile.setFolderId(folder.getParentId());
                    folderToFile.setFoldersByFolderId(folder.getFoldersByParentId());
                    folderToFile.setOwnerId(folder.getOwnerId());
                    folderToFile.setUsersByOwnerId(folder.getUsersByOwnerId());

                    FolderService folderService = new FolderService();
                    Pair<Timestamp, Integer> updatedFolderInfo = folderService.getLastModifiedInfo(folder.getId());
                    folderToFile.setUpdatedAt(updatedFolderInfo.getKey() != null? updatedFolderInfo.getKey() : null);

                    UserService userService = new UserService();
                    folderToFile.setUpdatedBy(updatedFolderInfo.getValue() == null ? null : updatedFolderInfo.getValue());
                    folderToFile.setUsersByUpdatedBy(updatedFolderInfo.getValue() == null ? null : userService.getUserById(updatedFolderInfo.getValue()));


                    int countItem = folderService.getNumberItemOfFolder(folder.getId());
                    folderToFile.setSize(Short.MIN_VALUE + countItem);

                    System.out.println("folderItem: " + folderToFile);
                    itemList.add(folderToFile);
                }
            }

            List<File> fileList = session.createQuery("select f from File f where f.folderId = :folderId", File.class)
                    .setParameter("folderId", folderId)
                    .list();
            System.out.println("fileList: " + fileList);
            if(fileList != null) {
                itemList.addAll(fileList);
            }

            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
