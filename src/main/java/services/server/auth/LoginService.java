package services.server.auth;

import DTO.UserSession;
import controllers.login.Encryptor;
import models.User;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class LoginService {
    Encryptor encryptor = new Encryptor();
    byte[] encryptionKey = {65, 12, 12, 12, 12, 12, 12, 12, 12,
            12, 12, 12, 12, 12, 12, 12};
    public LoginService() {
    }
    public UserSession validate(String username, String password) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            String encryptedPassword = encryptor.encrypt(password, encryptionKey);
            User user = session.createQuery("select u from User u where u.username = ?, u.password = ?", User.class)
                    .setParameter(0, username)
                    .setParameter(1, encryptedPassword)
                    .uniqueResult();
            if (user == null) {
                return null;
            } else {
                return new UserSession(user.getId(), user.getName(), user.getRole(), user.getAvatar());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
