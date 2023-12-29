package DTO;

public class UserSession {
    private int userId;
    private String name;
    private short roleId;
    private String avatar;

    public UserSession() {
    }

    public UserSession(int userId, String name, short roleId, String avatar) {
        this.userId = userId;
        this.name = name;
        this.roleId = roleId;
        this.avatar = avatar;
    }

    public void createSession(int id, String name, short roleId, String avatar) {
        this.userId = id;
        this.name = name;
        this.roleId = roleId;
        this.avatar = avatar;
    }
    public void destroySession() {
        userId = 0;
        name = null;
        roleId = 0;
        avatar = null;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getRoleId() {
        return roleId;
    }

    public void setRoleId(short roleId) {
        this.roleId = roleId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
