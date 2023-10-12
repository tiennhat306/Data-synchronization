package models;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "types", schema = "pbl4")
public class Type {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "name", unique = true, nullable = false)
    private String name;
    @Basic
    @Column(name = "image")
    private String image;
    @OneToMany(mappedBy = "typesByTypeId")
    private Collection<File> filesById;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return id == type.id && Objects.equals(name, type.name) && Objects.equals(image, type.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, image);
    }

    public Collection<File> getFilesById() {
        return filesById;
    }

    public void setFilesById(Collection<File> filesById) {
        this.filesById = filesById;
    }
}
