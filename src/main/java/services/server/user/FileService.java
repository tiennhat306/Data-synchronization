package services.server.user;

import applications.ServerApp;
import enums.FolderTypeId;
import enums.PermissionType;
import jakarta.persistence.NoResultException;
import models.File;
import models.Folder;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import applications.ServerApp;
import jakarta.persistence.NoResultException;
import models.File;
import utils.HibernateUtil;

public class FileService {
    public FileService() {
    }

    public static boolean checkFileExist(String fileName, int folderId) {
        if(fileName == null || fileName.isEmpty()) return false;
        if(folderId == -1) return false;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                int indexOfDot = fileName.indexOf(".");
                String nameOfFile = fileName.substring(0, indexOfDot);
                String typeOfFile = fileName.substring(indexOfDot + 1);

                int typeId = new TypeService().getTypeId(typeOfFile);

                return session.createQuery("select count(*) from File f where f.name = :nameOfFile AND f.typeId = :typeId AND f.folderId = :folderId", Long.class)
                        .setParameter("nameOfFile", nameOfFile)
                        .setParameter("typeId", typeId)
                        .setParameter("folderId", folderId)
                        .getSingleResult() > 0;
            } catch (NoResultException e) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkFileExistInPath(String fileName, int folderId) {
        if(folderId == -1) return false;
        String path = FolderService.getFolderPath(folderId) + java.io.File.separator + fileName;
        java.io.File file = new java.io.File(path);
        return file.exists();
    }

    public static void deleteFileInPath(String fileName, int folderId) {
        if(folderId == -1) return;
        String path = FolderService.getFolderPath(folderId) + java.io.File.separator + fileName;
        java.io.File file = new java.io.File(path);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileInPath(int fileId) {
        String path = getFilePath(fileId);

        java.io.File file = new java.io.File(path);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void moveToTrash(int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            File file = session.find(File.class, itemId);

            String filePath = ServerApp.SERVER_PATH + java.io.File.separator + file.getFinalpath() + java.io.File.separator + file.getName() + "." + TypeService.getTypeName(file.getTypeId());
            String trashPath = getFilePath(itemId);
            java.io.File fileToTrash = new java.io.File(filePath);
            java.io.File trashFile = new java.io.File(trashPath);
            if (!trashFile.getParentFile().exists()) {
                Files.createDirectories(trashFile.getParentFile().toPath());
            }

            System.out.println("File to trash: " + fileToTrash.getAbsolutePath());
            System.out.println("Trash file: " + trashFile.getAbsolutePath());

            Files.move(fileToTrash.toPath(), trashFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void restoreFileInPath(int itemId, String finalPath) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            File file = session.find(File.class, itemId);
            finalPath = ServerApp.SERVER_PATH + java.io.File.separator + finalPath + java.io.File.separator + file.getName() + "." + TypeService.getTypeName(file.getTypeId());
            java.io.File fileToRestore = new java.io.File(getFilePath(itemId));
            java.io.File restoreFile = new java.io.File(finalPath);
            if (!fileToRestore.getParentFile().exists()) {
                Files.createDirectories(fileToRestore.getParentFile().toPath());
            }
            Files.move(restoreFile.toPath(), fileToRestore.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void moveFileInPath(String beforePath, int targetId) {
        try {
            String fileName = beforePath.substring(beforePath.lastIndexOf(java.io.File.separator) + 1);
            String targetPath = FolderService.getFolderPath(targetId) + java.io.File.separator + fileName;
            java.io.File fileToMove = new java.io.File(beforePath);
            java.io.File targetFile = new java.io.File(targetPath);
            if (!targetFile.getParentFile().exists()) {
                try {
                    Files.createDirectories(targetFile.getParentFile().toPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(beforePath.equals(targetPath)) return;
            System.err.println("Move file from: " + beforePath);
            System.err.println("Move file to: " + targetPath);
            Files.move(fileToMove.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void renameFileInPath(int itemId, String beforePath) {
        try {
            String targetPath = getFilePath(itemId);
            java.io.File fileToMove = new java.io.File(beforePath);
            java.io.File targetFile = new java.io.File(targetPath);
            if (!targetFile.getParentFile().exists()) {
                try {
                    Files.createDirectories(targetFile.getParentFile().toPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Files.move(fileToMove.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFileInPath(String beforePath, int targetId) {
        try {
            String fileName = beforePath.substring(beforePath.lastIndexOf(java.io.File.separator) + 1);
            String targetPath = FolderService.getFolderPath(targetId) + java.io.File.separator + fileName;
            java.io.File fileToMove = new java.io.File(beforePath);
            java.io.File targetFile = new java.io.File(targetPath);
            if (!targetFile.getParentFile().exists()) {
                try {
                    Files.createDirectories(targetFile.getParentFile().toPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(beforePath.equals(targetPath)) return;
            System.err.println("Copy file from: " + beforePath);
            System.err.println("Copy file to: " + targetPath);
            Files.copy(fileToMove.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileIfExist(String pathInTrash) {
        java.io.File file = new java.io.File(pathInTrash);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getParentId(int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                return session.createQuery("select f.folderId from File f where f.id = :itemId", Integer.class)
                        .setParameter("itemId", itemId)
                        .getSingleResult();
            } catch (NoResultException e) {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean checkFileNameExist(String newName, int itemId) {
        if(newName == null || newName.isEmpty()) return false;
        if(itemId == -1) return false;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                File file = session.find(File.class, itemId);
                if(file == null) return false;
                int typeId = file.getTypeId();

                int parentFolderId = file.getFolderId();

                return session.createQuery("select count(*) from File f where f.name = :newName AND f.typeId = :typeId AND f.folderId = :parentFolderId AND f.id <> :itemId", Long.class)
                        .setParameter("newName", newName)
                        .setParameter("typeId", typeId)
                        .setParameter("parentFolderId", parentFolderId)
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
    
    public int getFileIDByName(String name, int parentID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Integer fileId = session.createQuery("select f.id from File f where f.name = :name and f.folderId = :parentID", Integer.class)
                    .setParameter("name", name)
                    .setParameter("parentID", parentID)
                    .uniqueResult();

            return (fileId != null) ? fileId : -1; // If fileId is null, return -1 (file not found)
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
    
    public boolean renameFile(int fileId, String newName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                File file = session.find(File.class, fileId);
                int typeId = file.getTypeId();
                int parentFolderId = file.getFolderId();
                boolean isDeletedSameFile = deleteSameFileIfExist(newName, typeId, parentFolderId);
                if(!isDeletedSameFile) {
                    return false;
                }

                if (file != null) {
                    file.setName(newName);
                    transaction.commit();
                    return true;
                } else {
                    return false;
                }
            } catch (NoResultException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public String getFilePathChanged(int fileId, String newName) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            int folderId = session.createQuery("select f.folderId from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult();

            String path = ServerApp.SERVER_PATH + java.io.File.separator + FolderService.getPath(folderId);

            String type = TypeService.getTypeName(session.createQuery("select f.typeId from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult());

            return path + java.io.File.separator + newName + "." + type;
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
    public boolean updateDeletedFile(int id, int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                File file = session.find(File.class, id);
                if(file == null) return false;
                file.setDeleted(true);
                file.setDeletedBy(userId);
                file.setDateDeleted(new Timestamp(System.currentTimeMillis()));
                session.merge(file);
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

    public boolean updateRestoredFile(int id){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
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

    public static boolean deleteFilePermanently(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                File file = session.find(File.class, id);
                if(file == null) return false;

                if (!PermissionService.deletePermissionByFileId(id)) {
                    return false;
                }

                if(!RecentFileService.deleteRecentFileByFileId(id)) {
                    return false;
                }

                session.remove(file);
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

    public boolean deleteSameFileIfExist(String name, int typeId, int folderId) {
        if(folderId == -1) return false;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                File file = session.createQuery("select f from File f where f.name = :name AND f.typeId = :typeId AND f.folderId = :folderId", File.class)
                        .setParameter("name", name)
                        .setParameter("typeId", typeId)
                        .setParameter("folderId", folderId)
                        .getSingleResult();

                if(file != null) {
                    String pathSameFile = FileService.getFilePath(file.getId());
                    boolean isDeletedInDB = FileService.deleteFilePermanently(file.getId());
                    if(isDeletedInDB){
                        FileService.deleteFileIfExist(pathSameFile);
                        transaction.commit();
                        return true;
                    } else {
                        transaction.rollback();
                        return false;
                    }
                }
                return true;
            } catch (NoResultException e) {
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

    public boolean copyFile(int id, int folderId) {
    	try(Session session = HibernateUtil.getSessionFactory().openSession()){
    		Transaction transaction = session.beginTransaction();
            try {
                File file = session.find(File.class, id);

                boolean isDeletedSameFile = deleteSameFileIfExist(file.getName(), file.getTypeId(), folderId);
                if(!isDeletedSameFile) {
                	return false;
                }

                File newfile = new File();
                // Set the properties of the File entity
                newfile.setName(file.getName());
                newfile.setTypeId(file.getTypeId());
                newfile.setFolderId(folderId);
                newfile.setOwnerId(file.getOwnerId());
                newfile.setSize(file.getSize());
                newfile.setCreatedAt(file.getCreatedAt());
                newfile.setUpdatedAt(file.getUpdatedAt());
                newfile.setUpdatedBy(file.getUpdatedBy());

                // Persist the File entity
                session.persist(newfile);
                session.getTransaction().commit();
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
    
    public boolean moveFile(int id, int folder_id) {
        Transaction transaction = null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            File file = session.find(File.class, id);

            boolean isDeletedSameFile = deleteSameFileIfExist(file.getName(), file.getTypeId(), folder_id);
            if(!isDeletedSameFile) {
                return false;
            }

            session.createQuery("delete from File f where f.name = :name AND f.typeId = :typeId AND f.folderId = :folderId")
                    .setParameter("name", file.getName())
                    .setParameter("typeId", file.getTypeId())
                    .setParameter("folderId", folder_id)
                    .executeUpdate();

            if (file != null) {
                // Update the file's name
                file.setFolderId(folder_id);
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

    public boolean uploadFile(String fileName, int fileType, int folderId, int ownerId, long size) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();

            File file = new File();

            file.setName(fileName);
            file.setTypeId(fileType);
            file.setFolderId(folderId);
            file.setOwnerId(ownerId);
            file.setSize(size);
            file.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            file.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            file.setUpdatedBy(ownerId);
            file.setDeleted(false);
            try {
                session.persist(file);
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

    public long sizeOfFile(int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("select f.size from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public String getPath(int fileId){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            int folderId = session.createQuery("select f.folderId from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult();

            String path = FolderService.getPath(folderId);

            File file = session.createQuery("select f from File f where f.id = :fileId", File.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult();

            TypeService typeService = new TypeService();
            String type = TypeService.getTypeName(session.createQuery("select f.typeId from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult());

            session.beginTransaction();
            file.setFinalpath(path);
            session.merge(file);
            session.getTransaction().commit();

            return path + java.io.File.separator + file.getName() + "." + type;
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static String getFilePath(int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            int folderId = session.createQuery("select f.folderId from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult();

            String path = FolderService.getFolderPath(folderId);

            String fileName = session.createQuery("select f.name from File f where f.id = :fileId", String.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult();

            String type = TypeService.getTypeName(session.createQuery("select f.typeId from File f where f.id = :fileId", Integer.class)
                    .setParameter("fileId", fileId)
                    .getSingleResult());

            System.err.println("Full file path: " + path + java.io.File.separator + fileName + "." + type);
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
            return file.getName() + "." + TypeService.getTypeName(file.getTypeId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getSize(int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            File file = session.find(File.class, fileId);
            if (file == null) return -1;
            return file.getSize();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getFileIdByFileNameAndFolderId(String fileName, int folderId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            int indexOfDot = fileName.indexOf(".");
            String nameOfFile = fileName.substring(0, indexOfDot);
            String typeOfFile = fileName.substring(indexOfDot + 1);

            int typeId = new TypeService().getTypeId(typeOfFile);

            return session.createQuery("select f.id from File f where f.name = :nameOfFile AND f.typeId = :typeId AND f.folderId = :folderId", Integer.class)
                    .setParameter("nameOfFile", nameOfFile)
                    .setParameter("typeId", typeId)
                    .setParameter("folderId", folderId)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean deleteFile(int itemId, int userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                File file = session.find(File.class, itemId);
                if(file == null) return false;
                file.setFinalpath(FolderService.getPath(file.getFolderId()));
                file.setFolderId(FolderTypeId.TRASH.getValue());
                session.merge(file);
                transaction.commit();
                return updateDeletedFile(itemId, userId);
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

    public boolean restoreFile(int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                File file = session.find(File.class, itemId);
                if(file == null) return false;

                String finalPath = file.getFinalpath();
                ResourceBundle resourceBundle = ResourceBundle.getBundle("application");
                String generalName = resourceBundle.getString("server.general.name");
                int generalId = FolderTypeId.GENERAL.getValue();

                if(finalPath.startsWith(generalName) && finalPath.contains(java.io.File.separator)) {
                    finalPath = finalPath.substring(generalName.length() + 1);
                    int index;
                    int folderId = generalId;
                    String folderName = "";
                    do {
                        index = finalPath.indexOf(java.io.File.separator);
                        folderName = finalPath.substring(0, index == -1 ? finalPath.length() : index);
                        int nextFolderId = FolderService.getFolderIdByPath(folderId, folderName);
                        if(folderId == -1) {
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
                            Folder folder = new Folder();
                            folder.setFolderName(folderName);
                            folder.setParentId(folderId);
                            folder.setOwnerId(file.getOwnerId());
                            folder.setDeleted(false);
                            session.persist(folder);
                            session.flush();
                            nextFolderId = folder.getId();
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

                    file.setFolderId(folderId);
                } else {
                    file.setFolderId(generalId);
                }

                session.merge(file);
                transaction.commit();
                return updateRestoredFile(itemId);
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

    public String getFinalPath(int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("select f.finalpath from File f where f.id = :itemId", String.class)
                    .setParameter("itemId", itemId)
                    .getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public String getFullNameById(int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            try {
                File file = session.find(File.class, itemId);
                if(file == null) return "";
                String nameOfFile = file.getName();
                String typeOfFile = TypeService.getTypeName(file.getTypeId());

                return nameOfFile + "." + typeOfFile;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
