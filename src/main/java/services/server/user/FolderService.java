package services.server.user;

import applications.ServerApp;
import enums.FolderTypeId;
import enums.PermissionType;
import enums.UploadStatus;
import jakarta.persistence.NoResultException;
import javafx.util.Pair;
import models.Folder;
import models.Permission;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import utils.HibernateUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.*;

public class FolderService {
    public FolderService() {
    }

    public static boolean checkFolderExistInPath(String folderName, int parentId) {
        if(parentId == -1) return false;
        String path = getFolderPath(parentId) + File.separator + folderName;
        File file = new File(path);
        return file.exists();
    }

    public static void deleteFolderInPath(String folderName, int parentId) {
        if(parentId == -1) return;
        String path = getFolderPath(parentId) + File.separator + folderName;
        deleteFolderIfExist(path);
    }

    public static void deleteFolderInPath(int folderId) {
        String path = getFolderPath(folderId);
        deleteFolderIfExist(path);
    }

    public static int getFolderIdByPath(int parentId, String folderName) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                return session.createQuery("select fd.id from Folder fd where fd.parentId = :parentId AND fd.folderName = :folderName", Integer.class)
                        .setParameter("parentId", parentId)
                        .setParameter("folderName", folderName)
                        .getSingleResult();
            } catch (NoResultException e) {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void restoreFolderInPath(int itemId, String finalPath) {
        String trashPath = getFolderPath(itemId);
        String trashToFolder = trashPath.substring(ServerApp.SERVER_PATH.length() + 1);
        trashToFolder = trashToFolder.replaceFirst("general", "trash");
        trashToFolder = ServerApp.SERVER_PATH + File.separator + trashToFolder;
        String folderName = trashPath.substring(trashPath.lastIndexOf(File.separator) + 1);
        finalPath = ServerApp.SERVER_PATH + File.separator + finalPath + File.separator + folderName;
        moveFolder(trashToFolder, finalPath);
    }

    public static String getFolderNameById(int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                Folder folder = session.find(Folder.class, itemId);
                if (folder == null) return null;
                return folder.getFolderName();
            } catch (NoResultException e) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void moveFolderInPath(String beforePath, int targetId) {
        try {
            String targetPath = getFolderPath(targetId);
            String folderName = beforePath.substring(beforePath.lastIndexOf(File.separator) + 1);
            targetPath = targetPath + File.separator + folderName;
            System.err.println("Move folder from: " + beforePath);
            System.err.println("Move file to: " + targetPath);

            if(beforePath.equals(targetPath)) return;

            moveFolder(beforePath, targetPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void renameFolderInPath(int itemId, String beforePath) {
        try {
            String targetPath = getFolderPath(itemId);
            File file = new File(beforePath);
            file.renameTo(new File(targetPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFolderInPath(String beforePath, int targetId) {
        try {
            String targetPath = getFolderPath(targetId);
            String folderName = beforePath.substring(beforePath.lastIndexOf(File.separator) + 1);
            targetPath = targetPath + File.separator + folderName;
            System.err.println("Copy folder from: " + beforePath);
            System.err.println("Copy folder to: " + targetPath);
            if(beforePath.equals(targetPath)) return;

            copyFolder(beforePath, targetPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getParentId(int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                Folder folder = session.find(Folder.class, itemId);
                if (folder == null) return -1;
                return folder.getParentId();
            } catch (NoResultException e) {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean checkFolderNameExist(String newName, int itemId) {
        if(itemId == -1) return false;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                int parentId = FolderService.getParentId(itemId);
                return session.createQuery("select count(*) from Folder fd where fd.folderName = :newName AND fd.parentId = :parentId AND fd.id <> :itemId", Long.class)
                        .setParameter("newName", newName)
                        .setParameter("parentId", parentId)
                        .setParameter("itemId", itemId)
                        .getSingleResult() > 0;
            } catch (NoResultException e) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Folder> getAllFolder() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select fd from Folder fd", Folder.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Folder getFolderById(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Folder.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Folder> getFoldersByParentId(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class).setParameter("id", id).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Folder> getFoldersByOwnerId(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select fd from Folder fd where fd.ownerId = :id", Folder.class).setParameter("id", id).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public long getSizeOfFolder(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select sum(f.size) from File f where f.folderId = :id", Long.class).setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public int getFolderIDByName(String name, int parentID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Integer folderId = session.createQuery("select f.id from Folder f where f.folderName = :name and f.parentId = :parentID", Integer.class)
                    .setParameter("name", name)
                    .setParameter("parentID", parentID)
                    .uniqueResult();

            return (folderId != null) ? folderId : -1; // If fileId is null, return -1 (file not found)
        } catch (NoResultException e) {
            // Handle the case where no result is found
            System.err.println("Warning: No file found with the specified name.");
            return -1;
        } catch (NonUniqueResultException e) {
            // Handle the case where there are multiple files with the same name
            System.err.println("Warning: Multiple files found with the same name.");
            return -1;
        } catch (HibernateException e) {
            e.printStackTrace(); // Log the exception or handle it appropriately
            return -1; // Handle the exception appropriately
        }
    }
    
    public int copyFolder(int id, int parentId, int permissionType) throws IOException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Folder folder = session.find(Folder.class, id);

            if (folder != null) {
                Folder newFolder = new Folder();
                newFolder.setFolderName(folder.getFolderName());
                newFolder.setParentId(parentId);
                newFolder.setOwnerId(folder.getOwnerId());
                newFolder.setDeleted(false);

                session.persist(newFolder);

                Permission permission = new Permission();
                permission.setFolderId(newFolder.getId());
                permission.setPermissionType((short) permissionType);
                session.persist(permission);

                transaction.commit();

                String path = ServerApp.SERVER_PATH + File.separator + getPath(newFolder.getId());
                createFolderIfNotExist(path);
                return newFolder.getId();
            } else {
                return -1;
            }
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return -1;
        }
    }


    
    public boolean moveFolder(int id, int folder_id) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Folder folder = session.find(Folder.class, id);

            boolean isDeletedSameFolder = deleteSameFolderIfExist(folder.getFolderName(), folder_id);
            if(!isDeletedSameFolder) return false;

            session.createQuery("delete from Folder fd where fd.folderName = :folderName AND fd.parentId = :parentId")
                    .setParameter("folderName", folder.getFolderName())
                    .setParameter("parentId", folder_id)
                    .executeUpdate();
            
            if (folder != null) {
                // Update the file's name
                folder.setParentId(folder_id);
                transaction.commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace(); // Handle the exception appropriately
            return false;
        }
    }
    public int getNumberItemOfFolder(int userId, int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            assert session != null;
            // add condition is id in list have permission
            String folderPermissionConditions = "((per.permissionType IN (2, 3) AND (per.userId is null OR per.userId = :userId)) OR fd.ownerId = :userId)";
            Query<Long> countFolder = session.createQuery("select count(*) from Folder fd join Permission per on fd.id = per.folderId " +
                    "WHERE fd.parentId = :id AND " + folderPermissionConditions, Long.class)
                    .setParameter("id", id)
                    .setParameter("userId", userId);
            String filePermissionConditions = "((per.permissionType IN (2, 3) AND (per.userId is null OR per.userId = :userId)) OR f.ownerId = :userId)";
            Query<Long> countFile = session.createQuery("select count(*) from File f join Permission per on f.folderId = per.folderId " +
                    "WHERE f.folderId = :id AND " + filePermissionConditions, Long.class)
                    .setParameter("id", id)
                    .setParameter("userId", userId);

            int fileCount = countFile.getSingleResult().intValue();
            int folderCount = countFolder.getSingleResult().intValue();

            return fileCount + folderCount;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public boolean renameFolder(int folderId, String newName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                boolean isDeletedSameFolder = deleteSameFolderIfExist(newName, FolderService.getParentId(folderId));
                if(!isDeletedSameFolder) return false;

                Folder folder = session.find(Folder.class, folderId);

                if (folder != null) {
                    folder.setFolderName(newName);
                    transaction.commit();
                    return true;
                } else {
                    return false;
                }
            } catch (NoResultException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Pair<Timestamp, Integer> getLastModifiedInfo(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Integer lastUpdatedPersonOfFiles = session.createQuery("select f.updatedBy from File f where f.folderId = :id order by f.updatedAt desc ", Integer.class)
                    .setParameter("id", id)
                    .setMaxResults(1)
                    .uniqueResult();
            Timestamp lastUpdatedTimeOfFiles = session.createQuery("select max(f.updatedAt) from File f where f.folderId = :id", Timestamp.class).setParameter("id", id).getSingleResult();

            List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class).setParameter("id", id).list();
            Integer lastUpdatedPersonOfFolders = null;
            Timestamp lastUpdatedTimeOfFolders = null;
            for (Folder folder : folderList) {
                Pair<Timestamp, Integer> updatedFolderInfo = getLastModifiedInfo(folder.getId());
                if (updatedFolderInfo == null || updatedFolderInfo.getKey() == null) continue;
                if (lastUpdatedTimeOfFolders == null || lastUpdatedTimeOfFolders.compareTo(updatedFolderInfo.getKey()) < 0) {
                    lastUpdatedTimeOfFolders = updatedFolderInfo.getKey();
                    lastUpdatedPersonOfFolders = updatedFolderInfo.getValue();
                }
            }

            if (lastUpdatedTimeOfFiles == null) {
                return new Pair<>(lastUpdatedTimeOfFolders, lastUpdatedPersonOfFolders);
            } else if (lastUpdatedTimeOfFolders == null) {
                return new Pair<>(lastUpdatedTimeOfFiles, lastUpdatedPersonOfFiles);
            } else {
                return lastUpdatedTimeOfFiles.compareTo(lastUpdatedTimeOfFolders) > 0 ? new Pair<>(lastUpdatedTimeOfFiles, lastUpdatedPersonOfFiles) : new Pair<>(lastUpdatedTimeOfFolders, lastUpdatedPersonOfFolders);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createFolderIfNotExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                Files.createDirectories(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteFolderIfExist(String path){
        File folder = new File(path);
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolderIfExist(file.getAbsolutePath());
                    } else {
                        try {
                            Files.deleteIfExists(file.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        try {
            Path pathFiles = folder.toPath();
            Files.deleteIfExists(pathFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getPath(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Folder folder = session.find(Folder.class, id);
            if (folder == null) return null;
            String folderName = folder.getFolderName();
            String path = folder.getFolderName();
            folder = folder.getFoldersByParentId();
            while(folder.getFoldersByParentId() != null ) {
                path = folder.getFolderName() + File.separator + path;
                folder = folder.getFoldersByParentId();
            }

            session.beginTransaction();
            folder.setFinalpath(path);
            session.merge(folder);
            session.getTransaction().commit();

            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int createFolder(String folderName, int ownerId, int parentId){
        return createFolder(folderName, ownerId, parentId, false);
    }

    public int createFolder(String folderName, int ownerId, int parentId, boolean isReplace){
        PermissionService permissionService = new PermissionService();
        int permissionType = permissionService.getPublicPermission(parentId, true);
        if (permissionType <= PermissionType.READ.getValue()) return UploadStatus.FAILED.getValue();

        String folderPath = ResourceBundle.getBundle("application").getString("server.path") + File.separator + folderName;
        boolean isExist = checkFolderExist(folderName, parentId);
        if (isExist) {
            if (isReplace) {
                boolean isDeletedInDB = deleteFolderPermanently(new FolderService().getFolderIdByNameAndParentId(folderName, parentId));
                if(isDeletedInDB){
                    FolderService.deleteFolderIfExist(folderPath);
                } else {
                    return UploadStatus.FAILED.getValue();
                }
            } else {
                return UploadStatus.EXISTED.getValue();
            }
        }

        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                Folder folder = new Folder();

                folder.setFolderName(folderName);
                folder.setOwnerId(ownerId);
                folder.setParentId(parentId);

                session.persist(folder);

                Permission permission = new Permission();
                permission.setFolderId(folder.getId());
                permission.setPermissionType((short) permissionType);

                session.persist(permission);

                transaction.commit();

                String path = ResourceBundle.getBundle("application").getString("server.path") + File.separator + getPath(folder.getId());
                createFolderIfNotExist(path);

                return UploadStatus.SUCCESS.getValue();
            } catch (Exception e) {
                e.printStackTrace();
                transaction.rollback();
                return UploadStatus.FAILED.getValue();
            }

        } catch (Exception e){
            e.printStackTrace();
            return UploadStatus.FAILED.getValue();
        }
    }

    private int getFolderIdByNameAndParentId(String folderName, int parentId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("select fd.id from Folder fd where fd.folderName = :folderName AND fd.parentId = :parentId", Integer.class)
                    .setParameter("folderName", folderName)
                    .setParameter("parentId", parentId)
                    .getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean checkFolderExist(String folderName, int parentId) {
        if(folderName == null || folderName.isEmpty()) return false;
        if(parentId == -1) return false;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("select count(*) from Folder fd where fd.folderName = :folderName AND fd.parentId = :parentId", Long.class)
                    .setParameter("folderName", folderName)
                    .setParameter("parentId", parentId)
                    .getSingleResult() > 0;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public int uploadFolder(int userId, String folderName, int ownerId, int parentId, int permissionType){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try{
                Folder folder = new Folder();
                folder.setFolderName(folderName);
                folder.setOwnerId(ownerId);
                folder.setParentId(parentId);
                folder.setDeleted(false);

                session.persist(folder);

                Permission permission = new Permission();
                permission.setFolderId(folder.getId());
                permission.setPermissionType((short) permissionType);

                session.persist(permission);

                session.getTransaction().commit();

                String path = ServerApp.SERVER_PATH + File.separator + getPath(folder.getId());
                createFolderIfNotExist(path);

                return folder.getId();
            } catch (Exception e){
                e.printStackTrace();
                transaction.rollback();
                return -1;
            }
        } catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public String getFolderName(int folderId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Folder folder = session.find(Folder.class, folderId);
            if (folder == null) return null;
            return folder.getFolderName();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFolderPath(int folderId) {
        return ServerApp.SERVER_PATH + File.separator + getPath(folderId);
    }


    public int getFolderId(String folderName, int currentFolderId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select fd.id from Folder fd where fd.folderName = :folderName AND fd.parentId = :currentFolderId", Integer.class)
                    .setParameter("folderName", folderName)
                    .setParameter("currentFolderId", currentFolderId)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updateDeletedFolder(int itemId, int userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Folder folder = session.find(Folder.class, itemId);
            if (folder == null) return false;

            folder.setDeleted(true);
            folder.setDeletedBy(userId);
            folder.setDateDeleted(new Timestamp(System.currentTimeMillis()));

            try {
                // recursively delete all sub folders and files
                List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class)
                        .setParameter("id", itemId)
                        .list();
                for (Folder subFolder : folderList) {
                    updateDeletedFolder(subFolder.getId(), userId);
                }
                List<models.File> fileList = session.createQuery("select f from File f where f.folderId = :id", models.File.class).setParameter("id", itemId).list();
                for (models.File file : fileList) {
                    FileService fileService = new FileService();
                    fileService.updateDeletedFile(file.getId(), userId);
                }

                session.merge(folder);
                transaction.commit();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRestoredFolder(int folderId){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                Folder folder = session.find(Folder.class, folderId);
                if (folder == null) return false;
                folder.setDeleted(false);
                folder.setDeletedBy(null);
                folder.setDateDeleted(null);
                folder.setFinalpath(null);

                // recursively restore all sub folders and files
                List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class)
                        .setParameter("id", folderId)
                        .list();
                for (Folder subFolder : folderList) {
                    updateRestoredFolder(subFolder.getId());
                }
                List<models.File> fileList = session.createQuery("select f from File f where f.folderId = :id", models.File.class)
                        .setParameter("id", folderId)
                        .list();
                for (models.File file : fileList) {
                    FileService fileService = new FileService();
                    fileService.updateRestoredFile(file.getId());
                }

                session.merge(folder);
                transaction.commit();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                transaction.rollback();
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFolderPermanently(int id){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            if (id == FolderTypeId.ROOT.getValue() || id == -1) return false;
            try {
                Folder folder = session.find(Folder.class, id);
                if (folder == null) return false;

                // recursively delete all sub folders and files
                List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class).setParameter("id", id).list();
                for (Folder subFolder : folderList) {
                    if(!deleteFolderPermanently(subFolder.getId())) {
                        return false;
                    }
                }
                List<models.File> fileList = session.createQuery("select f from File f where f.folderId = :id", models.File.class).setParameter("id", id).list();
                for (models.File file : fileList) {
                    if(!FileService.deleteFilePermanently(file.getId())) {
                        return false;
                    }
                }

                if(!PermissionService.deletePermissionByFolderId(id)) return false;

                session.remove(folder);
                transaction.commit();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                transaction.rollback();
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public int getFolderIdByFolderNameAndParentId(String folderName, int parentId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("select fd.id from Folder fd where fd.folderName = :folderName AND fd.parentId = :parentId", Integer.class)
                    .setParameter("folderName", folderName)
                    .setParameter("parentId", parentId)
                    .getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static void moveToTrash(int folderId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Folder folder = session.find(Folder.class, folderId);
            if (folder == null) return;

            String folderPath = ServerApp.SERVER_PATH + File.separator +  folder.getFinalpath() + File.separator + folder.getFolderName();
            String trashPath = ServerApp.SERVER_PATH + File.separator + getPath(folderId);

            moveFolder(folderPath, trashPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static void moveFolder(String folderPath, String targetPath) {
        System.err.println("From folder: " + folderPath);
        System.err.println("To folder: " + targetPath);

        File targetFolder = new File(targetPath);
        if (!targetFolder.exists()) {
            try {
                Files.createDirectories(targetFolder.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                String newFilePath = targetPath + File.separator + file.getName();
                if (file.isDirectory()) {
                    moveFolder(file.getAbsolutePath(), newFilePath);
                } else {
                    try {
                        Files.move(file.toPath(), new File(newFilePath).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        deleteFolderIfExist(folderPath);
    }

    private static void copyFolder(String folderPath, String targetPath) {
        System.err.println("From folder: " + folderPath);
        System.err.println("To folder: " + targetPath);

        File targetFolder = new File(targetPath);
        if (!targetFolder.exists()) {
            try {
                Files.createDirectories(targetFolder.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                String newFilePath = targetPath + File.separator + file.getName();
                if (file.isDirectory()) {
                    copyFolder(file.getAbsolutePath(), newFilePath);
                } else {
                    try {
                        Files.copy(file.toPath(), new File(newFilePath).toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean deleteFolder(int itemId, int userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Folder folder = session.find(Folder.class, itemId);
            if (folder == null) return false;

            try {
                folder.setFinalpath(FolderService.getPath(folder.getParentId()));
                folder.setParentId(FolderTypeId.TRASH.getValue());
                session.merge(folder);
                transaction.commit();
                return updateDeletedFolder(itemId, userId);
            } catch (Exception e) {
                e.printStackTrace();
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getFinalPath(int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Folder folder = session.find(Folder.class, itemId);
            if (folder == null) return null;
            return folder.getFinalpath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean restoreFolder(int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                Folder folder = session.find(Folder.class, itemId);
                if(folder == null) return false;

                String finalPath = folder.getFinalpath();
                ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
                String generalName = resourceBundle.getString("server.general.name");
                int generalId = FolderTypeId.GENERAL.getValue();
                System.out.println("Final Path: " + finalPath);

                if(finalPath.startsWith(generalName) && finalPath.contains(java.io.File.separator)) {
                    finalPath = finalPath.substring(generalName.length() + 1);
                    int index;
                    int folderId = generalId;
                    String folderName = "";
                    do {
                        index = finalPath.indexOf(java.io.File.separator);
                        folderName = finalPath.substring(0, index == -1 ? finalPath.length() : index);
                        int nextFolderId = FolderService.getFolderIdByPath(folderId, folderName);
                        if(nextFolderId == -1) {
                            break;
                        }
                        folderId = nextFolderId;
                        finalPath = finalPath.substring(index + 1);
                    } while (index != -1 && !finalPath.isEmpty());

                    while(!finalPath.isEmpty()) {

                        index = finalPath.indexOf(java.io.File.separator);
                        folderName = finalPath.substring(0, index == -1 ? finalPath.length() : index);
                        int nextFolderId = FolderService.getFolderIdByPath(folderId, folderName);
                        if(nextFolderId == -1) {
                            Folder newFolder = new Folder();
                            newFolder.setFolderName(folderName);
                            newFolder.setParentId(folderId);
                            newFolder.setOwnerId(folder.getOwnerId());
                            newFolder.setDeleted(false);
                            session.persist(newFolder);
                            session.flush();
                            nextFolderId = newFolder.getId();
                        }
                        if(nextFolderId == -1) {
                            return false;
                        }
                        folderId = nextFolderId;
                        if(index == -1) {
                            break;
                        } else {
                            finalPath = finalPath.substring(index + 1);
                        }
                    }

                    folder.setParentId(folderId);
                } else {
                    folder.setParentId(generalId);
                }

                session.merge(folder);
                transaction.commit();
                return updateRestoredFolder(itemId);
            } catch (Exception e) {
                e.printStackTrace();
                transaction.rollback();
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean copyFolder(int itemId, int targetId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                Folder folder = session.find(Folder.class, itemId);
                if(folder == null) return false;

                Folder targetFolder = session.find(Folder.class, targetId);
                if(targetFolder == null) return false;

                boolean isDeletedSameFolder = deleteSameFolderIfExist(folder.getFolderName(), targetId);
                if(!isDeletedSameFolder) return false;

                String folderName = folder.getFolderName();
                int ownerId = folder.getOwnerId();
                int permissionType = new PermissionService().getPublicPermission(targetId, true);

                Folder newFolder = new Folder();
                newFolder.setFolderName(folderName);
                newFolder.setParentId(targetId);
                newFolder.setOwnerId(ownerId);
                newFolder.setDeleted(false);

                session.persist(newFolder);
                session.flush();

                Permission permission = new Permission();
                permission.setFolderId(newFolder.getId());
                permission.setPermissionType((short) permissionType);

                session.persist(permission);

                session.getTransaction().commit();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                transaction.rollback();
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteSameFolderIfExist(String folderName, int targetId) {
        if (targetId == -1) return false;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                Folder folder = session.createQuery("select fd from Folder fd where fd.folderName = :folderName AND fd.parentId = :parentId", Folder.class)
                        .setParameter("folderName", folderName)
                        .setParameter("parentId", targetId)
                        .getSingleResult();
                if(folder == null) return true;
                else {
                    String sameFolderPath = FolderService.getFolderPath(folder.getId());
                    boolean isDeletedInDB =  new FolderService().deleteFolderPermanently(folder.getId());
                    if(isDeletedInDB){
                        FolderService.deleteFolderIfExist(sameFolderPath);
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (NoResultException e){
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                transaction.rollback();
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
