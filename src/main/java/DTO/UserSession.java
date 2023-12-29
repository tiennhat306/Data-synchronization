package DTO;

import java.util.Date;

public class UserDTO {
    private int userId;
    private String name;
    private int roleId;
    private String avatar;

    public UserDTO() {
    }

    public UserDTO(int userId, String name,int roleId, String avatar) {
        this.userId = userId;
        this.name = name;
        this.roleId = roleId;
        this.avatar = avatar;
    }

    public void createSession(int id, String name, int roleId, String avatar) {
        userId = id;
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

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
