package services.server.user;

import applications.ServerApp;
import models.File;
import models.RecentFile;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.sql.Timestamp;

public class RecentFileService {
    public boolean addRecentFile(int userId, int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();
            RecentFile recentFile = new RecentFile();
            recentFile.setUserId(userId);
            recentFile.setFileId(fileId);
            recentFile.setOpenedAt(new Timestamp(System.currentTimeMillis()));

            session.persist(recentFile);
            session.getTransaction().commit();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
