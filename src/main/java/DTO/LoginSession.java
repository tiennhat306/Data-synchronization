package DTO;

import java.util.Date;

public class LoginSession {
    private int currentUserID;
    private String currentUserName;
    private String currentUserUName;
    private String currentUserRole;
    private String currenUserMail;
    private String currenUserPhone;
    private Date currenUserBirth;
    private boolean currentUserGender;

    public LoginSession() {
        currentUserID = 0;
        currentUserName = null;
        currentUserUName = null;
        currentUserRole = null;
        currenUserMail = null;
        currenUserPhone = null;
        currenUserBirth = new Date();
        currentUserGender = false;
    }
    public void createSession(int id, String name, String username, String role, String mail, String phone, Date birth, boolean gender) {
        currentUserID = id;
        currentUserName = name;
        currentUserUName = username;
        currentUserRole = role;
        currenUserMail = mail;
        currenUserPhone = phone;
        currenUserBirth = birth;
        currentUserGender = gender;
    }
    public void destroySession() {
        currentUserID = 0;
        currentUserName = null;
        currentUserUName = null;
        currentUserRole = null;
        currenUserMail = null;
        currenUserPhone = null;
        currenUserBirth = new Date();
        currentUserGender = false;
    }
    public int getCurrentUserID() {
        return currentUserID;
    }
    public void setCurrentUserID(int currentUserID) {
        this.currentUserID = currentUserID;
    }
    public String getCurrentUserName() {
        return currentUserName;
    }
    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }
    public String getCurrentUserUName() {
        return currentUserUName;
    }
    public void setCurrentUserUName(String currentUserUName) {
        this.currentUserUName = currentUserUName;
    }
    public String getCurrentUserRole() {
        return currentUserRole;
    }
    public void setCurrentUserRole(String currentUserRole) {
        this.currentUserRole = currentUserRole;
    }

    public String getCurrenUserMail() {
        return currenUserMail;
    }

    public void setCurrenUserMail(String currenUserMail) {
        this.currenUserMail = currenUserMail;
    }

    public String getCurrenUserPhone() {
        return currenUserPhone;
    }

    public void setCurrenUserPhone(String currenUserPhone) {
        this.currenUserPhone = currenUserPhone;
    }

    public Date getCurrenUserBirth() {
        return currenUserBirth;
    }

    public void setCurrenUserBirth(Date currenUserBirth) {
        this.currenUserBirth = currenUserBirth;
    }

    public boolean isCurrentUserGender() {
        return currentUserGender;
    }

    public void setCurrentUserGender(boolean currentUserGender) {
        this.currentUserGender = currentUserGender;
    }
}
