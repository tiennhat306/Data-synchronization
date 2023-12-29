package services.server.auth;

import DTO.UserSession;
import utils.Encryptor;
import models.User;
import org.hibernate.Session;
import utils.HibernateUtil;

public class LoginService {
    Encryptor encryptor = new Encryptor();
    byte[] encryptionKey = {65, 12, 12, 12, 12, 12, 12, 12, 12,
            12, 12, 12, 12, 12, 12, 12};
    public LoginService() {
    }
    public UserSession validate(String username, String password) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            String encryptedPassword = encryptor.encrypt(password, encryptionKey);
            User user = session.createQuery("select u from User u where u.username = :username and u.password = :password", User.class)
                    .setParameter("username", username)
                    .setParameter("password", encryptedPassword)
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
