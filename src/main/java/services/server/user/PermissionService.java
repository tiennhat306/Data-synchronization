package services.server.user;

import jakarta.persistence.NoResultException;
import models.Permission;
import models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class PermissionService {
    public static final int FOLDER_TYPE = 1;
    public static final int PUBLIC_ACCESS = 3;
    public static final int OWNER_ACCESS = 3;
    public static final int READ_ACCESS = 2;
    public static final int PRIVATE_ACCESS = 1;

    public PermissionService() {

    }
    public List<Permission> getItemPermission(int userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Permission> permissionListList = session.createQuery("select per from Permission per where per.userId = :userId AND per.permissionType <> 0", Permission.class)
                    .setParameter("userId", userId)
                    .list();
            return permissionListList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean share(int itemTypeId, int itemId, int permissionType, int sharedBy, ArrayList<Integer> userList) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            for (Integer userId : userList) {
                Permission permission = new Permission();
                permission.setUserId(userId);
                if(itemTypeId == 1){
                    permission.setFolderId(itemId);
                } else {
                    permission.setFileId(itemId);
                }
                permission.setPermissionType((short) permissionType);
                permission.setSharedBy(sharedBy);
                permission.setSharedAt(new java.sql.Timestamp(System.currentTimeMillis()));
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

            boolean isOwner = session.createQuery("select (CASE WHEN count(*) > 0 THEN true ELSE false END) from " + (typeId == 1 ? "Folder" : "File") + " where id = :id AND ownerId = :userId", Boolean.class)
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (isOwner) {
                return OWNER_ACCESS;
            }

            int defaultPermission = getPermission(typeId, id);

            Permission userPermission = null;
            currentId = id;
            while (currentId != 1) {
                try{
                    userPermission = session.createQuery("select per from Permission per where per." + (typeId == 1 ? "folderId" : "fileId") + " = :id AND (per.userId = :userId)", Permission.class)
                            .setParameter("id", currentId)
                            .setParameter("userId", userId)
                            .uniqueResult();
                    if(userPermission == null){
                        throw new NoResultException();
                    }
                    else {
                        break;
                    }
                } catch (NoResultException e) {
                    Integer parentId = session.createQuery("select " + (typeId == 1 ? "fd.parentId" : "f.folderId") + " from " + (typeId == 1 ? "Folder fd" : "File f") + " where " + (typeId == 1 ? "fd.id" : "f.id") + " = :id", Integer.class)
                            .setParameter("id", currentId)
                            .uniqueResult();
                    if(parentId != null && parentId > 1){
                        currentId = parentId;
                    } else {
                        break;
                    }
                }
            }
            if (userPermission == null) {
                return defaultPermission;
            } else {
                return Math.max(defaultPermission, userPermission.getPermissionType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updatePermission(int itemTypeId, int itemId, int finalPermissionId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
//            String query = "select per from Permission per where per." + (itemTypeId == 1 ? "folderId" : "fileId") + " = :itemId AND per.userId = null";
            Permission permission;
            try{
//                permission = session.createQuery(query, Permission.class)
//                        .setParameter("itemId", itemId)
//                        .getSingleResult();
//                permission.setPermissionType((short) finalPermissionId);
//                session.merge(permission);
                if(itemTypeId != 1){
                    String query = "select per from Permission per where per.fileId = :itemId AND per.userId = null";
                    permission = session.createQuery(query, Permission.class)
                            .setParameter("itemId", itemId)
                            .getSingleResult();
                    if(permission == null){
                        throw new NoResultException();
                    } else {
                        permission.setPermissionType((short) finalPermissionId);
                        session.merge(permission);
                    }
                } else {
                    String nativeQuery = "WITH RECURSIVE folder_cte AS (\n" +
                            "  SELECT id FROM folders" +
                            "      WHERE id = :folderId\n" +
                            "  UNION ALL\n" +
                            "  SELECT f.id FROM folders f\n" +
                            "  INNER JOIN folder_cte AS fc ON f.parent_id = fc.id\n" +
                            ")\n" +
                            "UPDATE permissions SET permission_type = :permission WHERE folder_id IN (SELECT id FROM folder_cte)";
                    session.createNativeQuery(nativeQuery)
                            .setParameter("folderId", itemId)
                            .setParameter("permission", (short) finalPermissionId)
                            .executeUpdate();
                }
            } catch (NoResultException e) {
                permission = new Permission();
                permission.setUserId(null);
                if(itemTypeId == 1){
                    permission.setFolderId(itemId);
                } else {
                    permission.setFileId(itemId);
                }
                permission.setPermissionType((short) finalPermissionId);
                session.persist(permission);
            }
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
//            String query = "WITH RECURSIVE folder_cte AS (\n" +
//                    "  SELECT id FROM folders" +
//                    "      WHERE id = :folderId\n" +
//                    "  UNION ALL\n" +
//                    "  SELECT f.id FROM folders f\n" +
//                    "  INNER JOIN folder_cte AS fc ON f.parent_id = fc.id\n" +
//                    ")\n" +
//                    "UPDATE permissions SET permission_type = :permission WHERE folder_id IN (SELECT id FROM folder_cte)";
//            session.createNativeQuery(query)
//                    .setParameter("folderId", folderId)
//                    .setParameter("permission", PRIVATE_ACCESS)
//                    .executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPermission(int itemTypeId, int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Permission permission;
            try {
                permission = session.createQuery("select per from Permission per where per." + (itemTypeId == 1 ? "folderId" : "fileId") + " = :itemId AND per.userId = null", Permission.class)
                        .setParameter("itemId", itemId)
//                         .getSingleResult();
                        .uniqueResult();
                if(permission == null){
                    throw new NoResultException();
                } else {
                    return permission.getPermissionType();
                }
            } catch (NoResultException e) {
                int parentId = session.createQuery("select " + (itemTypeId == 1 ? "fd.parentId" : "f.folderId") + " from " + (itemTypeId == 1 ? "Folder fd" : "File f") + " where " + (itemTypeId == 1 ? "fd.id" : "f.id") + " = :id", Integer.class)
                        .setParameter("id", itemId)
                        .uniqueResult();
                return getPermission(PermissionService.FOLDER_TYPE, parentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<User> getSharedUser(int itemTypeId, int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            String query = "select u from User u where u.id in (select per.userId from Permission per where per." + (itemTypeId == 1 ? "folderId" : "fileId") + " = :itemId AND per.userId is not null)";
            return session.createQuery(query, User.class)
                    .setParameter("itemId", itemId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<User> searchUnsharedUser(int itemTypeId, int itemId, String searchText) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            String query = "select u from User u where u.id " +
                    "not in (select per.userId from Permission per where per." + (itemTypeId == 1 ? "folderId" : "fileId") + " = :itemId AND per.userId is not null)" +
                    " AND u.username like :searchText";
            return session.createQuery(query, User.class)
                    .setParameter("itemId", itemId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getOwnerId(int itemTypeId, int itemId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select " + (itemTypeId == 1 ? "fd.ownerId" : "f.ownerId") + " from " + (itemTypeId == 1 ? "Folder fd" : "File f") + " where " + (itemTypeId == 1 ? "fd.id" : "f.id") + " = :id", Integer.class)
                    .setParameter("id", itemId)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void deletePermissionByFileId(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createQuery("delete from Permission where fileId = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePermissionByFolderId(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createQuery("delete from Permission where folderId = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
