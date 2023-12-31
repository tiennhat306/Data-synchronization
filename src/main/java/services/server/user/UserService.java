package services.server.user;

import DTO.UserDTO;
import models.User;
import org.hibernate.Session;
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
            User user = session.find(User.class, id);
            return user.getUserPath();
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
}
