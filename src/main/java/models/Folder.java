package models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "folders", schema = "pbl4", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"folder_name", "parent_id"})
})
public class Folder implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "folder_name", nullable = false)
    private String folderName;
    @Basic
    @Column(name = "owner_id" ,nullable = false)
    private int ownerId;
    @Basic
    @Column(name = "parent_id")
    private Integer parentId;
    @OneToMany(mappedBy = "foldersByFolderId")
    private Collection<File> filesById;
    @ManyToOne(optional=false)
    @JoinColumn(name = "owner_id", referencedColumnName = "id", insertable=false, updatable=false)
    private User usersByOwnerId;
    @ManyToOne(optional=true)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", insertable=false, updatable=false)
    private Folder foldersByParentId;
    @OneToMany(mappedBy = "foldersByParentId")
    private Collection<Folder> foldersById;
    @OneToMany(mappedBy = "foldersByFolderId")
    private Collection<Permission> permissionsById;
    @Basic
    @Column(name = "is_deleted")
    private boolean isDeleted;
    @Basic
    @Column(name = "finalpath")
    private String finalpath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Collection<File> getFilesById() {
        return filesById;
    }

    public void setFilesById(Collection<File> filesById) {
        this.filesById = filesById;
    }

    public User getUsersByOwnerId() {
        return usersByOwnerId;
    }

    public void setUsersByOwnerId(User usersByOwnerId) {
        this.usersByOwnerId = usersByOwnerId;
    }

    public Folder getFoldersByParentId() {
        return foldersByParentId;
    }

    public void setFoldersByParentId(Folder foldersByParentId) {
        this.foldersByParentId = foldersByParentId;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getFinalpath() {
        return finalpath;
    }

    public void setFinalpath(String finalpath) {
        this.finalpath = finalpath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return id == folder.id && ownerId == folder.ownerId && isDeleted == folder.isDeleted && Objects.equals(folderName, folder.folderName) && Objects.equals(parentId, folder.parentId) && Objects.equals(finalpath, folder.finalpath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, folderName, ownerId, parentId, isDeleted, finalpath);
    }
}
