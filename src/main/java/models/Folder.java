package models;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "folders", schema = "pbl4", catalog = "")
public class Folder {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "folder_name")
    private String folderName;
    @Basic
    @Column(name = "owner_id")
    private int ownerId;
    @Basic
    @Column(name = "parent_id")
    private Integer parentId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return id == folder.id && ownerId == folder.ownerId && Objects.equals(folderName, folder.folderName) && Objects.equals(parentId, folder.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, folderName, ownerId, parentId);
    }
}
