package DTO;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Connection {
    private final SimpleStringProperty address;
    private final SimpleIntegerProperty port;
    public Connection(String address, int port) {
        this.address = new SimpleStringProperty(address);
        this.port = new SimpleIntegerProperty(port);
    }
    public Connection() {
        this.address = new SimpleStringProperty();
        this.port = new SimpleIntegerProperty();
    }
    public String getAddress() {
        return address.get();
    }
    public SimpleStringProperty addressProperty() {
        return address;
    }
    public void setAddress(String address) {
        this.address.set(address);
    }
    public int getPort() {
        return port.get();
    }
    public SimpleIntegerProperty portProperty() {
        return port;
    }
    public void setPort(int port) {
        this.port.set(port);
    }
}
