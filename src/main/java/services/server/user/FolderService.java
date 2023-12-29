package services.server.user;

import applications.ServerApp;
import javafx.util.Pair;
import models.Folder;
import models.Permission;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import utils.HibernateUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;

public class FolderService {
    public FolderService() {
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
    public int getNumberItemOfFolder(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            assert session != null;
            Query<Long> countFile = session.createQuery("select count(*) from File where folderId = :id", Long.class).setParameter("id", id);
            Query<Long> countFolder = session.createQuery("select count(*) from Folder where parentId = :id", Long.class).setParameter("id", id);

            int fileCount = countFile.getSingleResult().intValue();
            int folderCount = countFolder.getSingleResult().intValue();

            return fileCount + folderCount;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public boolean renameFolder(int id, String newName) {
        Transaction transaction = null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Folder folder = session.find(Folder.class, id);
            
            if (folder != null) {
                // Update the file's name
                folder.setFolderName(newName);
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

    public void deleteFolderIfExist(String path) throws IOException {
        File folder = new File(path);
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolderIfExist(file.getAbsolutePath());
                    } else {
                        Files.deleteIfExists(file.toPath());
                    }
                }
            }
        }
        Files.deleteIfExists(folder.toPath());
    }
    public String getPath(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Folder folder = session.find(Folder.class, id);
            if (folder == null) return null;
            String folderName = folder.getFolderName();
            String path = "";
            folder = folder.getFoldersByParentId();
            while(folder.getFoldersByParentId() != null ) {
                path = folder.getFolderName() + File.separator + path;
                folder = folder.getFoldersByParentId();
            }

            session.beginTransaction();
            folder.setFinalpath(path);
            session.merge(folder);
            session.getTransaction().commit();

            return path + folderName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createFolder(String folderName, int ownerId, int parentId){
        PermissionService permissionService = new PermissionService();
        int permissionType = permissionService.getPermission(1, parentId);
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();
            Folder folder = new Folder();

            folder.setFolderName(folderName);
            folder.setOwnerId(ownerId);
            folder.setParentId(parentId);

            session.persist(folder);

            Permission permission = new Permission();
            permission.setFolderId(folder.getId());
            permission.setPermissionType((short) permissionType);
            session.persist(permission);

            session.getTransaction().commit();

            String path = ResourceBundle.getBundle("application").getString("server.path") + File.separator + getPath(folder.getId());
            createFolderIfNotExist(path);

            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public int uploadFolder(String folderName, int ownerId, int parentId, int permissionType){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();
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
            deleteFolderIfExist(path);
            createFolderIfNotExist(path);

            return folder.getId();
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

    public String getFolderPath(int folderId) {
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

    public boolean deleteFolder(int itemId, int userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Folder folder = session.find(Folder.class, itemId);
            if (folder == null) return false;
            folder.setDeleted(true);
            folder.setDeletedBy(userId);
            folder.setDateDeleted(new Timestamp(System.currentTimeMillis()));
            folder.setFinalpath(getPath(folder.getParentId()));
            session.merge(folder);

            // recursively delete all subfolders and files
            List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class)
                    .setParameter("id", itemId)
                    .list();
            for (Folder subFolder : folderList) {
                deleteFolder(subFolder.getId(), userId);
            }
            List<models.File> fileList = session.createQuery("select f from File f where f.folderId = :id", models.File.class).setParameter("id", itemId).list();
            for (models.File file : fileList) {
                FileService fileService = new FileService();
                fileService.deleteFile(file.getId(), userId);
            }

            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreFolder(int folderId){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();
            Folder folder = session.find(Folder.class, folderId);
            if (folder == null) return false;
            folder.setDeleted(false);
            folder.setDeletedBy(null);
            folder.setDateDeleted(null);
            folder.setFinalpath(null);
            session.merge(folder);

            // recursively restore all subfolders and files
            List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class)
                    .setParameter("id", folderId)
                    .list();
            for (Folder subFolder : folderList) {
                restoreFolder(subFolder.getId());
            }
            List<models.File> fileList = session.createQuery("select f from File f where f.folderId = :id", models.File.class)
                    .setParameter("id", folderId)
                    .list();
            for (models.File file : fileList) {
                FileService fileService = new FileService();
                fileService.restoreFile(file.getId());
            }

            session.getTransaction().commit();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFolderPermanently(int id){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();
            Folder folder = session.find(Folder.class, id);
            if (folder == null) return false;

            // recursively delete all subfolders and files
            List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class).setParameter("id", id).list();
            for (Folder subFolder : folderList) {
                deleteFolderPermanently(subFolder.getId());
            }
            List<models.File> fileList = session.createQuery("select f from File f where f.folderId = :id", models.File.class).setParameter("id", id).list();
            for (models.File file : fileList) {
                FileService fileService = new FileService();
                fileService.deleteFilePermanently(file.getId());
            }

            deleteFolderIfExist(getFolderPath(id));
            PermissionService permissionService = new PermissionService();
            permissionService.deletePermissionByFolderId(id);

            session.remove(folder);

            session.getTransaction().commit();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
