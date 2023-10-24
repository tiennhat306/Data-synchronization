package services.admin;

import DTO.UserData;
import models.User;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;


public class UserService {
    private final Session session;
    public UserService() {
        this.session = null;
    }
    public UserService(Session session) {
        this.session = session;
    }
    public List<UserData> getAllUser() {
        try {
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
        try {
            return session.find(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

