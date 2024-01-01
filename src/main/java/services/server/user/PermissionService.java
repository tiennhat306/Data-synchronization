package services.server.user;

import DTO.UserToShareDTO;
import enums.PermissionType;
import enums.Role;
import jakarta.persistence.NoResultException;
import models.Permission;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class PermissionService {
    public PermissionService() {

    }
    public boolean share(int itemId, boolean isFolder, int permissionType, int sharedBy, ArrayList<Integer> userList) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            for (Integer userId : userList) {
                Permission permission = new Permission();
                permission.setUserId(userId);
                if(isFolder){
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

    public int checkUserPermission(int userId, int itemId, boolean isFolder) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            int currentId = itemId;

            boolean isOwner = session.createQuery("select (CASE WHEN count(*) > 0 THEN true ELSE false END) from " + (isFolder ? "Folder" : "File") + " where id = :id AND ownerId = :userId", Boolean.class)
                    .setParameter("id", itemId)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (isOwner) {
                return PermissionType.OWNER.getValue();
            }

            int defaultPermission = getPublicPermission(itemId, isFolder);

            Permission userPermission = null;
            while (currentId != 1) {
                try{
                    userPermission = session.createQuery("select per from Permission per where per." + (isFolder ? "folderId" : "fileId") + " = :id AND (per.userId = :userId)", Permission.class)
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
                    Integer parentId = session.createQuery("select " + (isFolder ? "fd.parentId" : "f.folderId") + " from " + (isFolder ? "Folder fd" : "File f") + " where " + (isFolder ? "fd.id" : "f.id") + " = :id", Integer.class)
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

    public boolean updatePublicPermission(int itemId, boolean isFolder, int finalPermissionId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
//            String query = "select per from Permission per where per." + (itemTypeId == 1 ? "folderId" : "fileId") + " = :itemId AND per.userId = null";

            try{
//                permission = session.createQuery(query, Permission.class)
//                        .setParameter("itemId", itemId)
//                        .getSingleResult();
//                permission.setPermissionType((short) finalPermissionId);
//                session.merge(permission);
                Permission permission;
                if(!isFolder){
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

                transaction.commit();
                return true;
            } catch (NoResultException e) {
                Permission per = new Permission();
                per.setUserId(null);
                if(isFolder){
                    per.setFolderId(itemId);
                } else {
                    per.setFileId(itemId);
                }
                per.setPermissionType((short) finalPermissionId);
                session.persist(per);
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

    public void addPermissionOfFolder(int folderId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Permission permission = new Permission();
            permission.setFolderId(folderId);
            permission.setPermissionType((short) PermissionType.PRIVATE.getValue());
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

    public int getPublicPermission(int itemId, boolean isFolder) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Permission permission;
            try {
                permission = session.createQuery("select per from Permission per where per." + (isFolder ? "folderId" : "fileId") + " = :itemId AND per.userId is null", Permission.class)
                        .setParameter("itemId", itemId)
                        .uniqueResult();
                if(permission == null){
                    throw new NoResultException();
                } else {
                    return permission.getPermissionType();
                }
            } catch (NoResultException e) {
                try {
                    int parentId = session.createQuery("select " + (isFolder ? "fd.parentId" : "f.folderId") + " from " + (isFolder ? "Folder fd" : "File f") + " where " + (isFolder ? "fd.id" : "f.id") + " = :id", Integer.class)
                            .setParameter("id", itemId)
                            .uniqueResult();
                    return getPublicPermission(parentId, isFolder);
                } catch (NoResultException ex) {
                    return PermissionType.PRIVATE.getValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<UserToShareDTO> getSharedUser(int itemId, boolean isFolder) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            int ownerId = getOwnerId(itemId, isFolder);
            return session.createQuery("select new DTO.UserToShareDTO(u.id, u.name, u.email) from User u" +
                            " where u.role <> :adminRole AND u.id <> :ownerId AND u.id in (select per.userId from Permission per where per." + (isFolder ? "folderId" : "fileId") + " = :itemId" +
                            " AND per.userId is not null)", UserToShareDTO.class)
                    .setParameter("adminRole", Role.ADMIN.getValue())
                    .setParameter("ownerId", ownerId)
                    .setParameter("itemId", itemId)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteSharedUser(int itemId, boolean isFolder, int userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.createQuery("delete from Permission where " + (isFolder ? "folderId" : "fileId") + " = :itemId AND userId = :userId")
                        .setParameter("itemId", itemId)
                        .setParameter("userId", userId)
                        .executeUpdate();
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

    public boolean updateSharedPermission(int itemId, boolean isFolder, int permissionType) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.createQuery("update Permission set permissionType = :permissionType where " + (isFolder ? "folderId" : "fileId") + " = :itemId AND userId is not null")
                        .setParameter("permissionType", (short) permissionType)
                        .setParameter("itemId", itemId)
                        .executeUpdate();
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

    public List<UserToShareDTO> searchUnsharedUser(int itemId, boolean isFolder, String searchText) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            int ownerId = getOwnerId(itemId, isFolder);

            return session.createQuery("select new DTO.UserToShareDTO(u.id, u.name, u.email) from User u" +
                            " where u.role <> :adminRole AND u.id <> :ownerId AND u.id not in (select per.userId from Permission per where per." + (isFolder? "folderId" : "fileId") + " = :itemId AND per.userId is not null)" +
                            " AND u.username like :searchText", UserToShareDTO.class)
                    .setParameter("adminRole", Role.ADMIN.getValue())
                    .setParameter("ownerId", ownerId)
                    .setParameter("itemId", itemId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getOwnerId(int itemId, boolean isFolder) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select " + (isFolder ? "fd.ownerId" : "f.ownerId") + " from " + (isFolder ? "Folder fd" : "File f") + " where " + (isFolder ? "fd.id" : "f.id") + " = :id", Integer.class)
                    .setParameter("id", itemId)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean deletePermissionByFileId(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.createQuery("delete from Permission where fileId = :id")
                        .setParameter("id", id)
                        .executeUpdate();
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

    public static boolean deletePermissionByFolderId(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.createQuery("delete from Permission where folderId = :id")
                        .setParameter("id", id)
                        .executeUpdate();
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

    public int checkSharedPermission(int itemId, boolean isFolder) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Permission permission;
            try {
                permission = session.createQuery("select per from Permission per where per." + (isFolder ? "folderId" : "fileId") + " = :itemId AND per.userId is not null", Permission.class)
                        .setParameter("itemId", itemId)
                        .setMaxResults(1)
                        .uniqueResult();
                if(permission == null) {
                    throw new NoResultException();
                }
                return permission.getPermissionType();
            } catch (NoResultException e) {
                return -1;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getSharedPermission(int itemId, boolean isFolder) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Permission permission;
            try {
                permission = session.createQuery("select per from Permission per where per." + (isFolder ? "folderId" : "fileId") + " = :itemId AND per.userId is not null", Permission.class)
                        .setParameter("itemId", itemId)
                        .setMaxResults(1)
                        .uniqueResult();
                if(permission == null) {
                    throw new NoResultException();
                }
                return permission.getPermissionType();
            } catch (NoResultException e) {
                return getPublicPermission(itemId, isFolder);
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
