package models;

import java.util.Date;

public class Item {
    private int id;
    private int type_id;
    private String name;
    private String owner_name;
    private Date dateModified;
    private String lastModifiedBy;
    private String size;

    public Item() {
    }

    public Item(int id, int type_id, String name, String owner_name, Date dateModified, String lastModifiedBy, String size) {
        this.id = id;
        this.type_id = type_id;
        this.name = name;
        this.owner_name = owner_name;
        this.dateModified = dateModified;
        this.lastModifiedBy = lastModifiedBy;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
