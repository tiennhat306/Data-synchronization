package DTO;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

public class RecentFileDTO implements Serializable {
    private int id;
    private int typeId;
    private String name;
    private String typeName;
    private int folderId;
    private Date openedDate;
    private String ownerName;
    private String path;
    private LinkedList<PathItem> pathItems;

    public RecentFileDTO() {
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

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public Date getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(Date openedDate) {
        this.openedDate = openedDate;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LinkedList<PathItem> getPathItems() {
        return pathItems;
    }

    public void setPathItems(LinkedList<PathItem> pathItems) {
        this.pathItems = pathItems;
    }

    public void addPathItem(PathItem pathItem) {
        if (pathItems == null) {
            pathItems = new LinkedList<>();
        }
        pathItems.add(pathItem);
    }
}
