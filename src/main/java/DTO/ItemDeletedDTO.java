package DTO;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

public class ItemDeletedDTO implements Serializable {
    private int id;
    private int typeId;
    private String name;
    private String typeName;
    private Date deletedDate;
    private String deletedPersonName;
    private int size;
    private String sizeName;
    private String beforeDeletedPath;
    private LinkedList<PathItem> path;
    public ItemDeletedDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Date getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }

    public String getDeletedPersonName() {
        return deletedPersonName;
    }

    public void setDeletedPersonName(String deletedPersonName) {
        this.deletedPersonName = deletedPersonName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getBeforeDeletedPath() {
        return beforeDeletedPath;
    }

    public void setBeforeDeletedPath(String beforeDeletedPath) {
        this.beforeDeletedPath = beforeDeletedPath;
    }

    public LinkedList<PathItem> getPath() {
        return path;
    }

    public void setPath(LinkedList<PathItem> path) {
        this.path = path;
    }

    public void addPathItem(PathItem pathItem) {
        if (path == null) {
            path = new LinkedList<>();
        }
        path.add(pathItem);
    }
}
