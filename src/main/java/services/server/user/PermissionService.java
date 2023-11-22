package services.server.user;

import models.Permission;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.util.List;

public class PermissionService {
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
}
