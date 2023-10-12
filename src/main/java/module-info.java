module com.datasynchronization.datasynchronization {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    opens applications to javafx.fxml;
    exports applications;
    opens controllers to javafx.fxml;
    exports controllers;
}