package models;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "permissions", schema = "pbl4", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "file_id", "folder_id"})
})
public class Permission {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "user_id", nullable = true)
    private Integer userId;
    @Basic
    @Column(name = "file_id", nullable = true)
    private Integer fileId;
    @Basic
    @Column(name = "folder_id", nullable = true)
    private Integer folderId;
    @Basic
    @Column(name = "permission_type", nullable = false)
    private short permissionType;
    @ManyToOne(optional=false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable=false, updatable=false)
    private User usersByUserId;
    @ManyToOne(optional=false)
    @JoinColumn(name = "file_id", referencedColumnName = "id", insertable=false, updatable=false)
    private File filesByFileId;
    @ManyToOne(optional=false)
    @JoinColumn(name = "folder_id", referencedColumnName = "id", insertable=false, updatable=false)
    private Folder foldersByFolderId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public short getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(short permissionType) {
        this.permissionType = permissionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return id == that.id && permissionType == that.permissionType && Objects.equals(userId, that.userId) && Objects.equals(fileId, that.fileId) && Objects.equals(folderId, that.folderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, fileId, folderId, permissionType);
    }

    public User getUsersByUserId() {
        return usersByUserId;
    }

    public void setUsersByUserId(User usersByUserId) {
        this.usersByUserId = usersByUserId;
    }

    public File getFilesByFileId() {
        return filesByFileId;
    }

    public void setFilesByFileId(File filesByFileId) {
        this.filesByFileId = filesByFileId;
    }

    public Folder getFoldersByFolderId() {
        return foldersByFolderId;
    }

    public void setFoldersByFolderId(Folder foldersByFolderId) {
        this.foldersByFolderId = foldersByFolderId;
    }
}
