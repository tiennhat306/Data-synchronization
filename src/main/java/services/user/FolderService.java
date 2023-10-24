package services.user;

import javafx.util.Pair;
import models.Folder;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;

public class FolderService {
    private final Session session;
    public FolderService() {
        this.session = null;
    }
    public FolderService(Session session) {
        this.session = session;
    }
    public List<Folder> getAllFolder() {
        try {
            return session.createQuery("select fd from Folder fd", Folder.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Folder getFolderById(int id) {
        try {
            return session.find(Folder.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Folder> getFoldersByParentId(int id) {
        try {
            return session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class).setParameter("id", id).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Folder> getFoldersByOwnerId(int id) {
        try {
            return session.createQuery("select fd from Folder fd where fd.ownerId = :id", Folder.class).setParameter("id", id).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public long getSizeOfFolder(int id) {
        try {
            return session.createQuery("select sum(f.size) from File f where f.folderId = :id", Long.class).setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public int getNumberItemOfFolder(int id) {
        try {
            assert session != null;
            Query<Integer> countFile = session.createQuery("select count(*) from File where folderId = :id", Integer.class).setParameter("id", id);
            Query<Integer> countFolder = session.createQuery("select count(*) from Folder where parentId = :id", Integer.class).setParameter("id", id);
            return countFile.getSingleResult() + countFolder.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

//    public Date getDateModified(int id) {
//        try {
//            Query<Date> query = session.createQuery("select max(f.updatedAt) from File f where f.folderId = :id", Date.class).setParameter("id", id);
//            Date dateOfFiles = query.getSingleResult();
//
//            List<Folder> folderList = session.createQuery("from Folder where parentId = :id", Folder.class).setParameter("id", id).list();
//            Date dateOfFolders = null;
//            for (Folder folder : folderList) {
//                Date date = getDateModified(folder.getId());
//                if (dateOfFolders == null || dateOfFolders.compareTo(date) < 0) {
//                    dateOfFolders = date;
//                }
//            }
//
//            return dateOfFiles == null ? dateOfFolders : dateOfFiles.compareTo(dateOfFolders) > 0 ? dateOfFiles : dateOfFolders;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public Pair<Date, Integer> getLastModifiedInfo(int id) {
        try {
            Integer updatedBy = session.createQuery("select f.updatedBy from File f where f.folderId = :id order by f.updatedAt desc ", Integer.class)
                    .setParameter("id", id)
                    .setMaxResults(1)
                    .uniqueResult();
            Date dateOfFiles = session.createQuery("select max(f.updatedAt) from File f where f.folderId = :id", Date.class).setParameter("id", id).getSingleResult();

            List<Folder> folderList = session.createQuery("select fd from Folder fd where fd.parentId = :id", Folder.class).setParameter("id", id).list();
            Integer updatedByFolder = null;
            Date dateOfFolders = null;
            for (Folder folder : folderList) {
                Pair<Date, Integer> updatedFolderInfo = getLastModifiedInfo(folder.getId());
                if (updatedFolderInfo == null || updatedFolderInfo.getKey() == null || updatedFolderInfo.getValue() == null) continue;
                if (dateOfFolders == null || dateOfFolders.compareTo(updatedFolderInfo.getKey()) < 0) {
                    dateOfFolders = updatedFolderInfo.getKey();
                    updatedByFolder = updatedFolderInfo.getValue();
                }
            }

            if (dateOfFiles == null) {
                return new Pair<>(dateOfFolders, updatedByFolder);
            } else if (dateOfFolders == null) {
                return new Pair<>(dateOfFiles, updatedBy);
            } else {
                return dateOfFiles.compareTo(dateOfFolders) > 0 ? new Pair<>(dateOfFiles, updatedBy) : new Pair<>(dateOfFolders, updatedByFolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
