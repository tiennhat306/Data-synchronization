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
                    " where fd.parentId = :folderId AND fd.folderName LIKE :searchText" +
                    " AND (" + folderPermissionConditions + ")";
            List<Folder> folderList = session.createQuery(folderQuery, Folder.class)
                    .setParameter("folderId", folderId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .setParameter("userId", userId)
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

            String filePermissionConditions = "(per.permissionType IN (2, 3) AND (per.userId is null OR per.userId = :userId)) OR fl.ownerId = :userId";
            String fileQuery = "select distinct fl from File fl Join Permission per on fl.folderId = per.folderId" +
                    " where fl.folderId = :folderId AND fl.name LIKE :searchText" +
                    " AND (" + filePermissionConditions + ")";
            List<File> fileList = session.createQuery(fileQuery, File.class)
                    .setParameter("folderId", folderId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .setParameter("userId", userId)
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

    public List<File> getAllItemPrivateOwnerId(int id, String searchText){
        int parentId = 1;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<File> itemList = new ArrayList<>();
//            List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.ownerId = :id and fd.parentId = :parentId", Folder.class)
//                    .setParameter("id", id)
//                    .setParameter("parentId", parentId)
//                    .list();
            List<Folder> folderList = session.createQuery("SELECT fd FROM Folder fd WHERE fd.ownerId = :id AND fd.parentId = :parentId AND fd.folderName LIKE :searchText", Folder.class)
                    .setParameter("id", id)
                    .setParameter("parentId", parentId)
                    .setParameter("searchText", "%" + searchText + "%")
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

            List<File> fileList = session.createQuery("select f from File f where f.ownerId = :id and f.folderId = :parentId AND f.name LIKE :searchText", File.class)
                    .setParameter("id", id)
                    .setParameter("parentId", parentId)
                    .setParameter("searchText", "%" + searchText + "%")
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
    public List<File> getAllOtherShareItem(int id, String searchText){
        Map<Integer, Pair<Integer, Integer>> mapFd = MapUtil.getMapFolder();
        Map<Integer, Pair<Integer, Integer>> mapFi = MapUtil.getMapFile();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            PermissionService permissionService = new PermissionService();
            FolderService folderService = new FolderService();
            FileService fileService = new FileService();
            List<Permission> permissionList = permissionService.getItemPermission(id);
            System.out.println(permissionList);
            List<File> itemList = new ArrayList<>();
            List<Folder> folderList = new ArrayList<>();
            List<File> fileList = new ArrayList<>();
            for (Permission permission : permissionList) {
                Integer folderId = permission.getFolderId();
                Integer fileId = permission.getFileId();
                System.out.println(folderId + " " + fileId);
                if (folderId != null) {
                    Pair<Integer, Integer> value = mapFd.get(folderId);
                    Integer secondValue = value.getValue();
                    if (secondValue != id) {
                        String nameFolder = folderService.getFolderName(folderId);
                        if (nameFolder.contains(searchText)) {
                            folderList.add(folderService.getFolderById(folderId));
                        }
                    }
                } else {
                    Pair<Integer, Integer> value = mapFi.get(fileId);
                    Integer secondValue = value.getValue();
                    if (secondValue != id) {
                        String nameFile = fileService.getFileName(fileId);
                        if (nameFile.contains(searchText)) {
                            fileList.add(fileService.getFileById(fileId));
                        }
                    }
                }
            }
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

            System.out.println("fileList: " + fileList);
            if(fileList != null) {
                itemList.addAll(fileList);
            }

            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
//        List<File> ret = new ArrayList<>();
//        return ret;
    }
    public List<File> getAllSharedItem(int id, String searchText){
        Map<Integer, Pair<Integer, Integer>> mapFd = MapUtil.getMapFolder();
        Map<Integer, Pair<Integer, Integer>> mapFi = MapUtil.getMapFile();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            PermissionService permissionService = new PermissionService();
            FolderService folderService = new FolderService();
            FileService fileService = new FileService();
            List<Permission> permissionList = permissionService.getItemPermission(id);
            System.out.println(permissionList);
            List<File> itemList = new ArrayList<>();
            List<Folder> folderList = new ArrayList<>();
            List<File> fileList = new ArrayList<>();
            for (Permission permission : permissionList) {
                Integer folderId = permission.getFolderId();
                Integer fileId = permission.getFileId();
                System.out.println(folderId + " " + fileId);
                if (folderId != null) {
                    Pair<Integer, Integer> value = mapFd.get(folderId);
                    Integer secondValue = value.getValue();
                    if (secondValue == id) {
                        String nameFolder = folderService.getFolderName(folderId);
                        if (nameFolder.contains(searchText)) {
                            folderList.add(folderService.getFolderById(folderId));
                        }
                    }
                } else {
                    Pair<Integer, Integer> value = mapFi.get(fileId);
                    Integer secondValue = value.getValue();
                    if (secondValue == id) {
                        String nameFile = fileService.getFileName(fileId);
                        if (nameFile.contains(searchText)) {
                            fileList.add(fileService.getFileById(fileId));
                        }
                    }
                }
            }
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

            System.out.println("fileList: " + fileList);
            if(fileList != null) {
                itemList.addAll(fileList);
            }

            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
//        List<File> ret = new ArrayList<>();
//        return ret;
    }
}
