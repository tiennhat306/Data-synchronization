package DTO;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Date;

public class UserData {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty gender;
    private final SimpleObjectProperty<Date> birthday;
    private final SimpleStringProperty phoneNumber;
    private final SimpleStringProperty email;
    private final SimpleStringProperty role;

    public UserData(int id, String name, String gender, Date birthday, String phoneNumber, String email, String role, int status) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.gender = new SimpleStringProperty(gender);
        this.birthday = new SimpleObjectProperty<>(birthday);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
    }
    public UserData(){
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.gender = new SimpleStringProperty();
        this.birthday = new SimpleObjectProperty<>();
        this.phoneNumber = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.role = new SimpleStringProperty();
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

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getGender() {
        return gender.get();
    }

    public SimpleStringProperty genderProperty() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender.set(gender);
    }

    public Date getBirthday() {
        return birthday.get();
    }

    public SimpleObjectProperty<Date> birthdayProperty() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday.set(birthday);
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public SimpleStringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    public String getEmail() {
        return email.get();
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getRole() {
        return role.get();
    }

    public SimpleStringProperty roleProperty() {
        return role;
    }

    public void setRole(String role) {
        this.role.set(role);
    }
}
