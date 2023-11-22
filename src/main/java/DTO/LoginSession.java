package DTO;
public class LoginSession {
    private int currentUserID;
    private String currentUserName;
    private String currentUserUName;
    private String currentUserRole;

    public LoginSession() {
        currentUserID = 0;
        currentUserName = null;
        currentUserUName = null;
        currentUserRole = null;
    }
    public void createSession(int id, String name, String username, String role) {
        currentUserID = id;
        currentUserName = name;
        currentUserUName = username;
        currentUserRole = role;
    }
    public void destroySession() {
        currentUserID = 0;
        currentUserName = null;
        currentUserUName = null;
        currentUserRole = null;
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
}
