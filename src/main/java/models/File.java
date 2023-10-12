package models;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "files", schema = "pbl4", catalog = "")
public class File {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "type_id")
    private int typeId;
    @Basic
    @Column(name = "folder_id")
    private int folderId;
    @Basic
    @Column(name = "owner_id")
    private int ownerId;
    @Basic
    @Column(name = "content")
    private byte[] content;
    @Basic
    @Column(name = "size")
    private Integer size;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Basic
    @Column(name = "updated_by")
    private Integer updatedBy;

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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return id == file.id && typeId == file.typeId && folderId == file.folderId && ownerId == file.ownerId && Objects.equals(name, file.name) && Arrays.equals(content, file.content) && Objects.equals(size, file.size) && Objects.equals(createdAt, file.createdAt) && Objects.equals(updatedAt, file.updatedAt) && Objects.equals(updatedBy, file.updatedBy);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, name, typeId, folderId, ownerId, size, createdAt, updatedAt, updatedBy);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }
}
