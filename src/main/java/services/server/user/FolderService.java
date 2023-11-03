package services.server.user;

import javafx.util.Pair;
import models.Folder;
import org.hibernate.Session;
import org.hibernate.query.Query;
import utils.HibernateUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

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
}
