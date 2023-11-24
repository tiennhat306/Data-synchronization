package services.server.user;

import jakarta.persistence.NoResultException;
import models.Permission;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class PermissionService {
    private static final int PUBLIC_ACCESS = 3;
    private static final int OWNER_ACCESS = 3;
    private static final int READ_ACCESS = 2;
    private static final int PRIVATE_ACCESS = 1;

    public PermissionService() {

    }
    public List<Permission> getItemPermission(int userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Permission> permissionListList = session.createQuery("select per from Permission per where per.userId = :userId AND per.permissionType <> 0", Permission.class)
                    .setParameter("userId", userId)
                    .list();
            System.out.println("permissionListList: " + permissionListList);
            return permissionListList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean shareFolder(int folderId, int permissionType, int sharedBy, ArrayList<Integer> userList) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            for (Integer userId : userList) {
                Permission permission = new Permission();
                permission.setUserId(userId);
                permission.setFolderId(folderId);
                permission.setPermissionType((short) permissionType);
                permission.setSharedBy(sharedBy);
                session.persist(permission);
            }
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean shareFile(int itemId, int permissionType, int sharedBy, ArrayList<Integer> userList) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            for (Integer userId : userList) {
                Permission permission = new Permission();
                permission.setUserId(userId);
                permission.setFileId(itemId);
                permission.setPermissionType((short) permissionType);
                permission.setSharedBy(sharedBy);
                session.persist(permission);
            }
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int checkPermission(int userId, int typeId, int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            int currentId = id;
            int currentTypeId = typeId;

            boolean isOwner = session.createQuery("select (CASE WHEN count(*) > 0 THEN true ELSE false END) from " + (currentTypeId == 1 ? "File" : "Folder") + " where id = :id AND ownerId = :userId", Boolean.class)
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (isOwner) {
                return OWNER_ACCESS;
            }

            Permission permission = null;
            while (currentId != 1) {
                try{
                    Short perrmissionType = session.createQuery("select per.permissionType from Permission per where per.userId = null AND per." + (currentTypeId == 1 ? "folderId" : "fileId") + " = :id", Short.class)
                            .setParameter("id", currentId)
                            .uniqueResult();
                    if (perrmissionType != null && perrmissionType != 0) {
                        return perrmissionType;
                    }

                    permission = session.createQuery("select per from Permission per where per.userId = :userId AND per." + (currentTypeId == 1 ? "folderId" : "fileId") + " = :id", Permission.class)
                            .setParameter("userId", userId)
                            .setParameter("id", currentId)
                            .getSingleResult();
                    if (permission != null) {
                        return permission.getPermissionType();
                    }

                } catch (NoResultException e) {
//                    if (currentTypeId == 1) {
//                        currentId = session.find(models.Folder.class, currentId).getParentId();
//                    } else {
//                        currentId = session.find(models.File.class, currentId).getFolderId();
//                    }
                    Integer parentId = session.createQuery("select " + (currentTypeId == 1 ? "fd.parentId" : "fl.folderId") + " from " + (currentTypeId == 1 ? "Folder fd" : "File fl") + " where " + (currentTypeId == 1 ? "fd.id" : "fl.id") + " = :id", Integer.class)
                            .setParameter("id", currentId)
                            .uniqueResult();
                    if(parentId != null && parentId > 1){
                        currentId = parentId;
                    } else {
                        break;
                    }
                }
            }
            return permission == null ? -1 : permission.getPermissionType();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updatePermission(int itemTypeId, int itemId, int finalPermissionId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Permission permission = session.createQuery("select per from Permission per where per." + (itemTypeId == 1 ? "folderId" : "fileId") + " = :itemId AND per.userId = null", Permission.class)
                    .setParameter("itemId", itemId)
                    .getSingleResult();
            permission.setPermissionType((short) finalPermissionId);
            session.merge(permission);
            transaction.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void addPermissionOfFile(int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Permission permission = new Permission();
            permission.setFileId(fileId);
            permission.setPermissionType((short) PRIVATE_ACCESS);
            session.persist(permission);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPermissionOfFolder(int folderId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Permission permission = new Permission();
            permission.setFolderId(folderId);
            permission.setPermissionType((short) PRIVATE_ACCESS);
            session.persist(permission);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
