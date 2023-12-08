package services.server.user;

import jakarta.persistence.NoResultException;
import models.RecentFile;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RecentFileService {
    public boolean addRecentFile(int userId, int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();

            try{
                RecentFile existRecentFile = session.createQuery("select rf from RecentFile rf where rf.userId = :userId and rf.fileId = :fileId", RecentFile.class)
                        .setParameter("userId", userId)
                        .setParameter("fileId", fileId)
                        .uniqueResult();
                if(existRecentFile != null){
                    existRecentFile.setOpenedAt(new Timestamp(System.currentTimeMillis()));
                    session.merge(existRecentFile);
                    session.getTransaction().commit();
                    return true;
                } else {
                    throw new NoResultException();
                }
            } catch (NoResultException e) {
                RecentFile recentFile = new RecentFile();
                recentFile.setUserId(userId);
                recentFile.setFileId(fileId);
                recentFile.setOpenedAt(new Timestamp(System.currentTimeMillis()));
                session.persist(recentFile);
                session.getTransaction().commit();
                return true;
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<RecentFile> getAllRecentOpenedItem(int userId, String searchText) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            List<RecentFile> recentFiles = session.createQuery("select rf from RecentFile rf where rf.userId = :userId and rf.filesByFileId.name LIKE :searchText and rf.filesByFileId.isDeleted = false order by rf.openedAt desc", RecentFile.class)
                    .setParameter("userId", userId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .setMaxResults(20)
                    .getResultList();
            if(recentFiles != null){
                for(RecentFile rf : recentFiles){
                    rf.getFilesByFileId().setFinalpath(new FolderService().getPath(rf.getFilesByFileId().getFolderId()));
                }
            }
            return recentFiles;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
