package DTO;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Date;
import java.util.LinkedHashMap;

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
    public UserData(LinkedHashMap<String, Object> userData){
        this.id = new SimpleIntegerProperty((int)userData.get("id"));
        this.name = new SimpleStringProperty((String) userData.get("name"));
        this.gender = new SimpleStringProperty((String)userData.get("gender"));
        this.birthday = new SimpleObjectProperty<>((Date) userData.get("birthday"));
        this.phoneNumber = new SimpleStringProperty((String)userData.get("phoneNumber"));
        this.email = new SimpleStringProperty((String)userData.get("email"));
        this.role = new SimpleStringProperty((String)userData.get("role"));
    }

    public LinkedHashMap<String, Object> getUserData(){
        LinkedHashMap<String, Object> userData = new LinkedHashMap<>();

        userData.put("id", this.id.get());
        userData.put("name", this.name.get());
        userData.put("gender", this.gender.get());
        userData.put("birthday", this.birthday.get());
        userData.put("phoneNumber", this.phoneNumber.get());
        userData.put("email", this.email.get());
        userData.put("role", this.role.get());

        return userData;
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
