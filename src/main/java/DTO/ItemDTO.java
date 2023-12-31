package DTO;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

public class ItemDTO implements Serializable {
    private int id;
    private int typeId;
    private String name;
    private String typeName;
    private int ownerId;
    private String ownerName;
    private Date updatedDate;
    private String updatedPersonName;
    private int size;
    private String sizeName;

    private LinkedList<PathItem> path;

    public ItemDTO() {
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

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedPersonName() {
        return updatedPersonName;
    }

    public void setUpdatedPersonName(String updatedPersonName) {
        this.updatedPersonName = updatedPersonName;
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
        path.add(0, pathItem);
    }
}
