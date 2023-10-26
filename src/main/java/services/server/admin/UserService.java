package services.server.admin;

import DTO.UserData;
import models.User;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.List;


public class UserService {
    public UserService() {
    }
    public List<UserData> getAllUser() {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            List<UserData> userDataList = new ArrayList<>();
            List<User> userList = session.createQuery("select u from User u where u.status = true", User.class).list();
            if(userList != null){
                for (User user : userList) {
                    UserData userdata = new UserData();
                    userdata.setId(user.getId());
                    userdata.setName(user.getName());
                    userdata.setGender(user.getGender() ? "Nam" : "Nữ"); // true 1 : Nam, false 0 : Nữ
                    userdata.setBirthday(user.getBirthday());
                    userdata.setPhoneNumber(user.getPhoneNumber());
                    userdata.setEmail(user.getEmail());
                    userdata.setRole(user.getRole() == 1 ? "Quản trị viên" : "Người dùng");
                    userDataList.add(userdata);
                }
            }
            return userDataList;
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

