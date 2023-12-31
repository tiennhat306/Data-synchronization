package services.server.admin;

import DTO.UserDTO;
import models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import utils.HibernateUtil;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;


public class UserService {
    public UserService() {
    }
    public List<UserDTO> getAllUser() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            List<UserDTO> userDTOList = new ArrayList<>();
            List<User> userList = session.createQuery("select u from User u where u.status = true", User.class).list();
            for (User user : userList) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setName(user.getName());
                userDTO.setEmail(user.getEmail());
                userDTO.setPhoneNumber(user.getPhoneNumber());
                userDTO.setBirthday(user.getBirthday());
                userDTO.setGender(user.isGender());
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
    public User getUserByUserName(String username) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUser(String username, String name, String email, String phone, Date birth, boolean gender) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            // Tìm người dùng dựa trên tên người dùng
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            User user = query.uniqueResult();
            if (user != null) {
                // Cập nhật thông tin người dùng
                user.setName(name);
                user.setEmail(email);
                user.setPhoneNumber(phone);
                user.setBirthday(birth);
                user.setGender(gender);
                session.update(user); // Thực hiện cập nhật
                transaction.commit(); // Lưu thay đổi vào cơ sở dữ liệu
                return true;
            } else {
                // Người dùng không tồn tại
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean changePassUser(String username, String newPass) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            // Tìm người dùng dựa trên tên người dùng
            Query<User> query = session.createQuery("FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            User user = query.uniqueResult();
            if (user != null) {
                // Cập nhật thông tin người dùng
                user.setPassword(newPass);
                session.update(user); // Thực hiện cập nhật
                transaction.commit(); // Lưu thay đổi vào cơ sở dữ liệu
                return true;
            } else {
                // Người dùng không tồn tại
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

