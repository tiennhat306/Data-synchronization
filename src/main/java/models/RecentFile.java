package models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "recentfiles", schema = "pbl4", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "file_id"})
})
public class RecentFile  implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "user_id")
    private int userId;
    @Basic
    @Column(name = "file_id")
    private int fileId;
    @Basic
    @Column(name = "opened_at")
    private Timestamp openedAt;
    @ManyToOne(optional=false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable=false, updatable=false)
    private User usersByUserId;
    @ManyToOne(optional=false)
    @JoinColumn(name = "file_id", referencedColumnName = "id", insertable=false, updatable=false)
    private File filesByFileId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public Timestamp getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Timestamp openedAt) {
        this.openedAt = openedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecentFile that = (RecentFile) o;
        return id == that.id && userId == that.userId && fileId == that.fileId && Objects.equals(openedAt, that.openedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, fileId, openedAt);
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
}
