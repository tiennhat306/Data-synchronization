package services.server.user;

import applications.ServerApp;
import models.File;
import models.Folder;
import models.Permission;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FileService {
    public FileService() {
    }
    public List<File> getAllFile() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select f from File f", File.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<File> getAllFileByFolderId(int folderId) {
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
    public boolean deleteFile(int id, int userId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            File file = session.find(File.class, id);
            if(file == null) return false;
            file.setDeleted(true);
            file.setDeletedBy(userId);
            file.setDateDeleted(new Timestamp(System.currentTimeMillis()));
            FolderService folderService = new FolderService();
            file.setFinalpath(folderService.getPath(file.getFolderId()));
            session.merge(file);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreFile(int id){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            File file = session.find(File.class, id);
            if(file == null) return false;
            file.setDeleted(false);
            file.setDeletedBy(null);
            file.setDateDeleted(null);
            file.setFinalpath(null);
            session.merge(file);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFilePermanently(int id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            File file = session.find(File.class, id);
            if(file == null) return false;
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

    /// The dat
    public boolean checkExistedFile(String name) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();

            String sqlQuery = "Select count(*) from files f where f.name = :name";
            int result = Integer.parseInt(session.createNativeQuery(sqlQuery, String.class).setParameter("name", name)
                    .getSingleResult().toString());
            session.getTransaction().commit();
            session.close();
            return result == 0;
        } catch (Exception e){
            e.printStackTrace();
            return true;
        }

    }

//    public int getFileTypeId(String typeName) {
//        try(Session session = HibernateUtil.getSessionFactory().openSession()){
//            return session.createQuery("select t.id from Type t where t.name = :name", Integer.class)
//                    .setParameter("name", typeName)
//                    .getSingleResult();
//        } catch (Exception e){
//            e.printStackTrace();
//            return 0;
//        }
//    }

    public List<String> getListFileName() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();

            List<String> result = new ArrayList<String>();

            String sqlQuery = "SELECT files.name FROM files";

            result = session.createNativeQuery(sqlQuery, String.class).getResultList();

            session.getTransaction().commit();
            session.close();
            return result;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }

    public List<String> updateListFileName() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();

            List<String> result = new ArrayList<String>();

            String sqlQuery = "SELECT files.name FROM files";

            result = session.createNativeQuery(sqlQuery, String.class).getResultList();

            session.getTransaction().commit();
            session.close();
            return result;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void deleteFileIfExist(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if(file.exists()){
            boolean rs = file.delete();
            if(rs){
                System.out.println("Xóa file thành công");
            } else {
                System.out.println("Xóa file thất bại");
            }
        }
    }

    public String uploadFile(String fileName, int fileType, int folderId, int ownerId, int size) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            session.beginTransaction();
            File file = new File();
            // Set the properties of the File entity
            file.setName(fileName);
            file.setTypeId(fileType);
            file.setFolderId(folderId);
            file.setOwnerId(ownerId);
            file.setSize(size);
            file.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            file.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            file.setUpdatedBy(ownerId);
            file.setDeleted(false);

            session.persist(file);
            session.getTransaction().commit();

            FolderService folderService = new FolderService();
            String path = ServerApp.SERVER_PATH + java.io.File.separator + folderService.getPath(folderId);
            folderService.createFolderIfNotExist(path);

            TypeService typeService = new TypeService();
            String filePath = path + java.io.File.separator + fileName + "." + typeService.getTypeName(fileType);
            deleteFileIfExist(filePath);
            return filePath;
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public String convertFileListToString(List<File> fileList) {
        StringBuilder builder = new StringBuilder();
        for (File file : fileList) {
            builder.append(file.getName()).append(';'); // Use a suitable delimiter
        }
        return builder.toString();
    }

    public int sizeOfFile(int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("select f.size from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public String getFilePath(int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            int folderId = session.createQuery("select f.folderId from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult();

            FolderService folderService = new FolderService();
            String path = ServerApp.SERVER_PATH + java.io.File.separator + folderService.getPath(folderId);

            String fileName = session.createQuery("select f.name from File f where f.id = :fileId", String.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult();

            TypeService typeService = new TypeService();
            String type = typeService.getTypeName(session.createQuery("select f.typeId from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult());

            return path + java.io.File.separator + fileName + "." + type;
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
    public String getFileName(int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            File file = session.find(File.class, fileId);
            if (file == null) return null;
            return file.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getFileId(String fileName, int fileTypeId, int currentFolderId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select f.id from File f where f.name = :fileName AND f.typeId = :fileTypeId AND f.folderId = :currentFolderId", Integer.class)
                    .setParameter("fileName", fileName)
                    .setParameter("fileTypeId", fileTypeId)
                    .setParameter("currentFolderId", currentFolderId)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getFullFileName(int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            File file = session.find(File.class, fileId);
            if (file == null) return null;
            return file.getName() + "." + new TypeService().getTypeName(file.getTypeId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getSize(int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            File file = session.find(File.class, fileId);
            if (file == null) return -1;
            return file.getSize();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
