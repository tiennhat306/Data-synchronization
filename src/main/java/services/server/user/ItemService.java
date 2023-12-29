package services.server.user;

import javafx.util.Pair;
import models.File;
import models.Folder;
import models.Permission;
import models.Type;
import org.hibernate.Session;
import utils.HibernateUtil;
import utils.MapUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ItemService {
    public ItemService() {
    }

    public List<File> getAllItem(int userId, int folderId, String searchText){
        PermissionService permissionService = new PermissionService();
        int permission = permissionService.checkPermission(userId, 1, folderId);
        if (permission == 1) {
            return null;
        }

        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<File> itemList = new ArrayList<>();

            String folderPermissionConditions = "(per.permissionType IN (2, 3) AND (per.userId is null OR per.userId = :userId)) OR fd.ownerId = :userId";
            String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                    " where fd.parentId = :folderId AND fd.folderName LIKE :searchText AND fd.isDeleted = false" +
                    " AND (" + folderPermissionConditions + ")";
            List<Folder> folderList = session.createQuery(folderQuery, Folder.class)
                    .setParameter("folderId", folderId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .setParameter("userId", userId)
                    .list();
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
                    itemList.add(folderToFile);
                }
            }

            String filePermissionConditions = "(per.permissionType IN (2, 3) AND (per.userId is null OR per.userId = :userId)) OR fl.ownerId = :userId";
            String fileQuery = "select distinct fl from File fl Join Permission per on fl.folderId = per.folderId" +
                    " where fl.folderId = :folderId AND fl.name LIKE :searchText AND fl.isDeleted = false" +
                    " AND (" + filePermissionConditions + ")";
            List<File> fileList = session.createQuery(fileQuery, File.class)
                    .setParameter("folderId", folderId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .setParameter("userId", userId)
                    .list();
            if(fileList != null) {
                itemList.addAll(fileList);
            }

            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<File> getAllItemPrivateOwnerId(int userId, String searchText){
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<File> itemList = new ArrayList<>();

            List<Folder> folderList;

            if(searchText.equals("")) {
                String folderQuery = "select distinct fd from Folder fd" +
                        " where fd.ownerId = :userId AND fd.isDeleted = false" +
                        " and fd.parentId NOT IN (SELECT fd2.id FROM Folder fd2" +
                        " where fd2.ownerId = :userId and fd2.isDeleted = false)";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String folderQuery = "select distinct fd from Folder fd" +
                        " where fd.ownerId = :userId AND fd.folderName LIKE :searchText AND fd.isDeleted = false";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

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
                    itemList.add(folderToFile);
                }
            }

            List<File> fileList;
            if(searchText.equals("")){
                String fileQuery = "select distinct fl from File fl" +
                        " where fl.ownerId = :userId AND fl.isDeleted = false" +
                        " AND fl.folderId NOT IN (SELECT fd.id FROM Folder fd" +
                        " WHERE fd.ownerId = :userId and fd.isDeleted = false)";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String fileQuery = "select distinct fl from File fl" +
                        " where fl.ownerId = :userId AND fl.name LIKE :searchText AND fl.isDeleted = false";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            if(fileList != null) {
                itemList.addAll(fileList);
            }

            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<File> getAllOtherShareItem(int userId, String searchText){
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<File> itemList = new ArrayList<>();

            List<Folder> folderList;
            if(searchText.equals("")) {
                String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                        " where fd.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId" +
                        " AND fd.parentId NOT IN (SELECT fd2.id FROM Folder fd2 Join Permission per2 on fd2.id = per2.folderId" +
                        " WHERE per2.userId = :userId AND per2.permissionType IN (2, 3) and fd2.isDeleted = false)";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                        " where fd.folderName LIKE :searchText AND fd.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

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
                    itemList.add(folderToFile);
                }
            }

            List<File> fileList;
            if(searchText.equals("")){
                String fileQuery = "select distinct f from File f Join Permission per on f.id = per.fileId" +
                        " where f.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId" +
                        " AND f.folderId NOT IN (SELECT fd.id FROM Folder fd Join Permission per on fd.id = per.folderId" +
                        " WHERE per.userId = :userId AND per.permissionType IN (2, 3) and fd.isDeleted = false)";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String fileQuery = "select distinct f from File f Join Permission per on f.id = per.fileId" +
                        " where f.name LIKE :searchText AND f.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            if(fileList != null) {
                itemList.addAll(fileList);
            }

            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<File> getAllSharedItem(int userId, String searchText){
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<File> itemList = new ArrayList<>();

            List<Folder> folderList;
            if(searchText.equals("")) {
                String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                        " where fd.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.sharedBy = :userId" +
                        " AND fd.parentId NOT IN (SELECT fd2.id FROM Folder fd2 Join Permission per2 on fd2.id = per2.folderId" +
                        " WHERE per2.sharedBy = :userId AND per2.permissionType IN (2, 3) and fd2.isDeleted = false)";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                        " where fd.folderName LIKE :searchText AND fd.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.sharedBy = :userId";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

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
                    itemList.add(folderToFile);
                }
            }

            List<File> fileList;
            if(searchText.equals("")){
                String fileQuery = "select distinct f from File f Join Permission per on f.id = per.fileId" +
                        " where f.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.sharedBy = :userId" +
                        " AND f.folderId NOT IN (SELECT fd.id FROM Folder fd Join Permission per on fd.id = per.folderId" +
                        " WHERE per.sharedBy = :userId AND per.permissionType IN (2, 3) and fd.isDeleted = false)";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String fileQuery = "select distinct f from File f Join Permission per on f.id = per.fileId" +
                        " where f.name LIKE :searchText AND f.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            if(fileList != null) {
                itemList.addAll(fileList);
            }

            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<File> getAllDeletedItem(int userId, String searchText) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<File> itemList = new ArrayList<>();

            List<Folder> folderList;
            if(searchText.equals("")){
                String folderQuery = "select distinct fd from Folder fd" +
                        " where fd.isDeleted = true AND fd.ownerId = :userId" +
                        " AND fd.parentId NOT IN (SELECT fd2.id FROM Folder fd2" +
                        " WHERE fd2.ownerId = :userId and fd2.isDeleted = true)";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String folderQuery = "select distinct fd from Folder fd" +
                        " where fd.folderName LIKE :searchText AND fd.isDeleted = true AND fd.ownerId = :userId";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            if (folderList != null) {
                for (Folder folder : folderList) {
                    File folderToFile = new File();
                    folderToFile.setId(folder.getId());
                    folderToFile.setName(folder.getFolderName());
                    folderToFile.setTypeId(1);
                    folderToFile.setFolderId(folder.getParentId());
                    folderToFile.setFoldersByFolderId(folder.getFoldersByParentId());
                    folderToFile.setOwnerId(folder.getOwnerId());
                    folderToFile.setUsersByOwnerId(folder.getUsersByOwnerId());

                    folderToFile.setDeleted(folder.isDeleted());
                    folderToFile.setDateDeleted(folder.getDateDeleted());
                    folderToFile.setDeletedBy(folder.getDeletedBy());
                    folderToFile.setUsersByDeletedBy(folder.getUsersByDeletedBy());
                    folderToFile.setFinalpath(folder.getFinalpath());

                    FolderService folderService = new FolderService();
                    Pair<Timestamp, Integer> updatedFolderInfo = folderService.getLastModifiedInfo(folder.getId());
                    folderToFile.setUpdatedAt(updatedFolderInfo.getKey() != null ? updatedFolderInfo.getKey() : null);

                    UserService userService = new UserService();
                    folderToFile.setUpdatedBy(updatedFolderInfo.getValue() == null ? null : updatedFolderInfo.getValue());
                    folderToFile.setUsersByUpdatedBy(updatedFolderInfo.getValue() == null ? null : userService.getUserById(updatedFolderInfo.getValue()));


                    int countItem = folderService.getNumberItemOfFolder(folder.getId());
                    folderToFile.setSize(Short.MIN_VALUE + countItem);
                    itemList.add(folderToFile);
                }
            }

            List<File> fileList;
            if(searchText.equals("")){
                String fileQuery = "select distinct f from File f" +
                        " where f.isDeleted = true AND f.ownerId = :userId" +
                        " AND f.folderId NOT IN (SELECT fd.id FROM Folder fd" +
                        " WHERE fd.ownerId = :userId and fd.isDeleted = true)";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String fileQuery = "select distinct f from File f" +
                        " where f.name LIKE :searchText AND f.isDeleted = true AND f.ownerId = :userId";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            if (fileList != null) {
                itemList.addAll(fileList);
            }

            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
