package DTO;

import java.util.Date;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Item {
    private final SimpleIntegerProperty id;
    private final SimpleIntegerProperty typeId;
    private final SimpleStringProperty name;
    private final SimpleStringProperty ownerName;
    private final SimpleObjectProperty<Date> dateModified;
    private final SimpleStringProperty lastModifiedBy;
    private final SimpleStringProperty size;

    public Item(int id, int typeId, String name, String ownerName, Date dateModified, String lastModifiedBy, String size) {
        this.id = new SimpleIntegerProperty(id);
        this.typeId = new SimpleIntegerProperty(typeId);
        this.name = new SimpleStringProperty(name);
        this.ownerName = new SimpleStringProperty(ownerName);
        this.dateModified = new SimpleObjectProperty<>(dateModified);
        this.lastModifiedBy = new SimpleStringProperty(lastModifiedBy);
        this.size = new SimpleStringProperty(size);
    }

    public Item() {
        this.id = new SimpleIntegerProperty();
        this.typeId = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.ownerName = new SimpleStringProperty();
        this.dateModified = new SimpleObjectProperty<>();
        this.lastModifiedBy = new SimpleStringProperty();
        this.size = new SimpleStringProperty();
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getTypeId() {
        return typeId.get();
    }

    public SimpleIntegerProperty typeIdProperty() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId.set(typeId);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getOwnerName() {
        return ownerName.get();
    }

    public SimpleStringProperty ownerNameProperty() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName.set(ownerName);
    }

    public Date getDateModified() {
        return dateModified.get();
    }

    public SimpleObjectProperty<Date> dateModifiedProperty() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified.set(dateModified);
    }

    public String getLastModifiedBy() {
        return lastModifiedBy.get();
    }

    public SimpleStringProperty lastModifiedByProperty() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy.set(lastModifiedBy);
    }

    public String getSize() {
        return size.get();
    }

    public SimpleStringProperty sizeProperty() {
        return size;
    }

    public void setSize(String size) {
        this.size.set(size);
    }
}
