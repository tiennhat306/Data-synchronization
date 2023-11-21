package services.server.user;

import applications.ServerApp;
import javafx.util.Pair;
import models.Folder;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import utils.HibernateUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    
    public boolean copyFolder(int id, int parentId) {
    	Transaction transaction = null;
    	try(Session session = HibernateUtil.getSessionFactory().openSession()){
    		transaction = session.beginTransaction();
    		Folder folder = session.find(Folder.class, id);
    		
    		Folder newfolder = new Folder();
            // Set the properties of the File entity
            newfolder.setFolderName(folder.getFolderName());
            newfolder.setParentId(parentId);
            newfolder.setOwnerId(folder.getOwnerId());

            // Persist the File entity
            session.persist(newfolder);
            session.getTransaction().commit();
            return true;
    	} catch (Exception e) {
			// TODO: handle exception
    		e.printStackTrace();
    		return false;
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
    
    public boolean moveFolder(int id, int folder_id) {
        Transaction transaction = null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Folder folder = session.find(Folder.class, id);
            
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
    
    public boolean deleteFolder(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Folder folder = session.find(Folder.class, id);
            session.remove(folder);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
            String path = folder.getFolderName();
            folder = folder.getFoldersByParentId();
            while(folder.getFoldersByParentId() != null ) {
                path = folder.getFolderName() + File.separator + path;
                folder = folder.getFoldersByParentId();
            }
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createFolder(String folderName, int ownerId, int parentId){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();
            Folder folder = new Folder();

            folder.setFolderName(folderName);
            folder.setOwnerId(ownerId);
            folder.setParentId(parentId);

            session.persist(folder);
            session.getTransaction().commit();

            String path = ServerApp.SERVER_PATH + File.separator + getPath(folder.getId());
            createFolderIfNotExist(path);

            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public int uploadFolder(String folderName, int ownerId, int parentId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();
            Folder folder = new Folder();

            folder.setFolderName(folderName);
            folder.setOwnerId(ownerId);
            folder.setParentId(parentId);

            session.persist(folder);
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
}
