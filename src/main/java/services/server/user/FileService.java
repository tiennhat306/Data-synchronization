package services.server.user;

import models.File;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.List;

public class FileService {
    public FileService() {
    }
    public List<File> getAllFile(int folderId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            //print session
            System.out.println("session: " + session);

            return session.createQuery("select f from File f where f.folderId = :folderId", File.class)
                    .setParameter("folderId", folderId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getFileById(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(File.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean addFile(File file) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(file);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateFile(File file) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.merge(file);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteFile(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            File file = session.find(File.class, id);
            session.remove(file);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<File> getFilesByOwnerId(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("select f from File f where f.ownerId = :userId", File.class)
                    .setParameter("userId", userId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<File> getFilesByFolderId(int folderId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select f from File f where f.folderId = :folderId", File.class)
                    .setParameter("folderId", folderId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<File> getFilesByType(int type_id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select f from File f where f.typeId = :type_id", File.class)
                    .setParameter("type_id", type_id)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
