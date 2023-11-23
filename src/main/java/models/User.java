package models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "users", schema = "pbl4")
public class User implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "name", nullable = false)
    private String name;
    @Basic
    @Column(name = "birthday", nullable = false)
    private Date birthday;
    @Basic
    @Column(name = "gender", nullable = false)
    private boolean gender;
    @Basic
    @Column(name = "avatar")
    private String avatar;
    @Basic
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    @Basic
    @Column(name = "password", nullable = false)
    private String password;
    @Basic
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;
    @Basic
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    @Basic
    @Column(name = "role", nullable = false)
    private short role;
    @Basic
    @Column(name = "status", nullable = false)
    private boolean status;
    @Basic
    @Column(name = "user_path")
    private String userPath;
    @Basic
    @Column(name = "refresh_token")
    private String refreshToken;
    @Basic
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
    @OneToMany(mappedBy = "usersByOwnerId")
    private Collection<File> filesById;
    @OneToMany(mappedBy = "usersByUpdatedBy")
    private Collection<File> filesById_0;
    @OneToMany(mappedBy = "usersByOwnerId")
    private Collection<Folder> foldersById;
    @OneToMany(mappedBy = "usersByUserId")
    private Collection<Permission> permissionsById;
    @OneToMany(mappedBy = "usersBySharedBy")
    private Collection<Permission> permissionsById_0;

    public boolean isGender() {
        return gender;
    }

    public boolean isStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public boolean getGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public short getRole() {
        return role;
    }

    public void setRole(short role) {
        this.role = role;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getUserPath() {
        return userPath;
    }

    public void setUserPath(String userPath) {
        this.userPath = userPath;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        UserData user = (UserData) o;
//        return id == user.id && gender == user.gender && role == user.role && status == user.status && Objects.equals(name, user.name) && Objects.equals(birthday, user.birthday) && Arrays.equals(avatar, user.avatar) && Objects.equals(username, user.username) && Objects.equals(password, user.password) && Objects.equals(phoneNumber, user.phoneNumber) && Objects.equals(email, user.email) && Objects.equals(userPath, user.userPath) && Objects.equals(refreshToken, user.refreshToken) && Objects.equals(createdAt, user.createdAt) && Objects.equals(updatedAt, user.updatedAt);
//    }

//    @Override
//    public int hashCode() {
//        int result = Objects.hash(id, name, birthday, gender, username, password, phoneNumber, email, role, status, userPath, refreshToken, createdAt, updatedAt);
//        result = 31 * result + Arrays.hashCode(avatar);
//        return result;
//    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, birthday, gender, avatar, username, password, phoneNumber, email, role, status, userPath, refreshToken, createdAt, updatedAt, filesById, filesById_0, foldersById, permissionsById);
    }

    public Collection<File> getFilesById() {
        return filesById;
    }

    public void setFilesById(Collection<File> filesById) {
        this.filesById = filesById;
    }

    public Collection<File> getFilesById_0() {
        return filesById_0;
    }

    public void setFilesById_0(Collection<File> filesById_0) {
        this.filesById_0 = filesById_0;
    }

    public Collection<Folder> getFoldersById() {
        return foldersById;
    }

    public void setFoldersById(Collection<Folder> foldersById) {
        this.foldersById = foldersById;
    }

    public Collection<Permission> getPermissionsById() {
        return permissionsById;
    }

    public void setPermissionsById(Collection<Permission> permissionsById) {
        this.permissionsById = permissionsById;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && gender == user.gender && role == user.role && status == user.status && Objects.equals(name, user.name) && Objects.equals(birthday, user.birthday) && Objects.equals(avatar, user.avatar) && Objects.equals(username, user.username) && Objects.equals(password, user.password) && Objects.equals(phoneNumber, user.phoneNumber) && Objects.equals(email, user.email) && Objects.equals(userPath, user.userPath) && Objects.equals(refreshToken, user.refreshToken) && Objects.equals(createdAt, user.createdAt) && Objects.equals(updatedAt, user.updatedAt);
    }

    public Collection<Permission> getPermissionsById_0() {
        return permissionsById_0;
    }

    public void setPermissionsById_0(Collection<Permission> permissionsById_0) {
        this.permissionsById_0 = permissionsById_0;
    }
}
