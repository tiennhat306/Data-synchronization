package services.server.admin;

import models.User;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class UserService {
    public UserService() {
    }
    public List<User> getAllUser() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            List<LinkedHashMap<String, Object>> userLists = new ArrayList<>();
            List<User> userList = session.createQuery("select u from User u where u.status = true", User.class).list();
            return userList;
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

