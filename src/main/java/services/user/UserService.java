package services.user;

import models.User;
import org.hibernate.Session;

import java.util.List;


public class UserService {
    private final Session session;
    public UserService() {
        this.session = null;
    }
    public UserService(Session session) {
        this.session = session;
    }
    public List<User> getAllUser() {
        try {
            return session.createQuery("select u from User u", User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public User getUserById(int id) {
        try {
            return session.find(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
