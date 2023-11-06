package services.server.user;

import models.User;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.util.List;


public class UserService {
    public UserService() {
    }
    public List<User> getAllUser() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select u from User u", User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public User getUserById(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
