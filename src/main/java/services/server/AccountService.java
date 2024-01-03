package services.server;

import DTO.UserAccountDTO;
import org.hibernate.Transaction;
import utils.Encryptor;
import models.User;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.sql.Date;

public class AccountService {
    Encryptor encryptor = new Encryptor();
    byte[] encryptionKey = {65, 12, 12, 12, 12, 12, 12, 12, 12,
            12, 12, 12, 12, 12, 12, 12};
    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                oldPassword = encryptor.encrypt(oldPassword, encryptionKey);
                User user = session.createQuery("select u from User u where u.id = :userId and u.password = :password", User.class)
                        .setParameter("userId", userId)
                        .setParameter("password", oldPassword)
                        .uniqueResult();
                if (user == null) {
                    return false;
                } else {
                    newPassword = encryptor.encrypt(newPassword, encryptionKey);
                    System.out.println("new password: " + newPassword);
                    user.setPassword(newPassword);
                    session.beginTransaction();
                    session.merge(user);
                    session.getTransaction().commit();
                    return true;
                }
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public UserAccountDTO getUserAccountInfo(int userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            User user = session.createQuery("select u from User u where u.id = :userId", User.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
            if (user == null) {
                return null;
            } else {
                UserAccountDTO userAccountDTO = new UserAccountDTO();
                userAccountDTO.setUsername(user.getUsername());
                userAccountDTO.setName(user.getName());
                userAccountDTO.setEmail(user.getEmail());
                userAccountDTO.setPhoneNumber(user.getPhoneNumber());
                userAccountDTO.setBirthday(user.getBirthday());
                userAccountDTO.setGender(user.isGender());
                return userAccountDTO;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUserInfo(int userId, String username, String name, String email, String phone, Date birthday, boolean gender) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            try {
                User user = session.find(User.class, userId);
                user.setUsername(username);
                user.setName(name);
                user.setEmail(email);
                user.setPhoneNumber(phone);
                user.setBirthday(birthday);
                user.setGender(gender);
                session.merge(user);
                transaction.commit();
                return true;
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
