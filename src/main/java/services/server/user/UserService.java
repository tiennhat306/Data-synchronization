package services.server.user;

import DTO.UserDTO;
import jakarta.persistence.NoResultException;
import models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.List;


public class UserService {
    public UserService() {
    }
    public List<UserDTO> getAllUser() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> userList = session.createQuery("select u from User u", User.class).list();
            if(userList == null) return null;
            List<UserDTO> userDTOList = new ArrayList<>();
            for(User user : userList) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setName(user.getName());
                userDTO.setGender(user.isGender());
                userDTO.setBirthday(user.getBirthday());
                userDTO.setPhoneNumber(user.getPhoneNumber());
                userDTO.setEmail(user.getEmail());
                userDTO.setRole(user.getRole());
                userDTOList.add(userDTO);
            }
            return userDTOList;
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

    public String getUserPath(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                User user = session.find(User.class, id);
                return user.getUserPath();
            } catch (NoResultException e) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getNameOfUserById(int id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            String query = "select u.name from User u where u.id = :id";
            return session.createQuery(query, String.class).setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUserPath(int userId, String text) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                User user = session.find(User.class, userId);
                user.setUserPath(text);
                session.merge(user);
                session.getTransaction().commit();
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
