module com.datasynchronization.datasynchronization {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;

    requires org.kordamp.bootstrapfx.core;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.apache.commons.net;
    requires de.jensd.fx.glyphs.fontawesome;
    requires com.google.gson;

    opens models to org.hibernate.orm.core, javafx.base, com.google.gson; // Mở gói 'models' cho Hibernate

    opens applications to javafx.fxml;
    exports applications;
    opens controllers to javafx.fxml;
    exports controllers;
    opens controllers.user to javafx.fxml;
    exports controllers.user;
    opens controllers.admin to javafx.fxml;
    exports controllers.admin;
    opens controllers.server to javafx.fxml;
    exports controllers.server;
    opens DTO to javafx.base, org.hibernate.orm.core, com.google.gson;

}