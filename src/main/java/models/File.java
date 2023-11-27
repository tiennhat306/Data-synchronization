package models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "files", schema = "pbl4", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "type_id", "folder_id"})
})
public class File implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "name", nullable = false)
    private String name;
    @Basic
    @Column(name = "type_id" ,nullable = false)
    private int typeId;
    @Basic
    @Column(name = "folder_id" ,nullable = false)
    private int folderId;
    @Basic
    @Column(name = "owner_id" ,nullable = false)
    private int ownerId;
    @Basic
    @Column(name = "size")
    private Integer size;
    @Basic
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Basic
    @Column(name = "updated_by")
    private Integer updatedBy;
    @ManyToOne(optional=false)
    @JoinColumn(name = "type_id", referencedColumnName = "id", insertable=false, updatable=false)
    private Type typesByTypeId;
    @ManyToOne(optional=false)
    @JoinColumn(name = "folder_id", referencedColumnName = "id", insertable=false, updatable=false)
    private Folder foldersByFolderId;
    @ManyToOne(optional=false)
    @JoinColumn(name = "owner_id", referencedColumnName = "id", insertable=false, updatable=false)
    private User usersByOwnerId;
    @ManyToOne(optional=false)
    @JoinColumn(name = "updated_by", referencedColumnName = "id", insertable=false, updatable=false)
    private User usersByUpdatedBy;
    @OneToMany(mappedBy = "filesByFileId")
    private Collection<Permission> permissionsById;
    @Basic
    @Column(name = "is_deleted")
    private boolean isDeleted;

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

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
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

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Type getTypesByTypeId() {
        return typesByTypeId;
    }

    public void setTypesByTypeId(Type typesByTypeId) {
        this.typesByTypeId = typesByTypeId;
    }

    public Folder getFoldersByFolderId() {
        return foldersByFolderId;
    }

    public void setFoldersByFolderId(Folder foldersByFolderId) {
        this.foldersByFolderId = foldersByFolderId;
    }

    public User getUsersByOwnerId() {
        return usersByOwnerId;
    }

    public void setUsersByOwnerId(User usersByOwnerId) {
        this.usersByOwnerId = usersByOwnerId;
    }

    public User getUsersByUpdatedBy() {
        return usersByUpdatedBy;
    }

    public void setUsersByUpdatedBy(User usersByUpdatedBy) {
        this.usersByUpdatedBy = usersByUpdatedBy;
    }

    public Collection<Permission> getPermissionsById() {
        return permissionsById;
    }

    public void setPermissionsById(Collection<Permission> permissionsById) {
        this.permissionsById = permissionsById;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
