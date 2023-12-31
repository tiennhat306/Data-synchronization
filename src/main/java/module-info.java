module com.datasynchronization.datasynchronization {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;

    requires org.kordamp.bootstrapfx.core;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.apache.commons.net;
    requires de.jensd.fx.glyphs.fontawesome;
    requires java.desktop;

    opens models to org.hibernate.orm.core, javafx.base; // Mở gói 'models' cho Hibernate

    opens applications to javafx.fxml;
    exports applications;
    opens controllers.user to javafx.fxml;
    exports controllers.user;
    opens controllers.admin to javafx.fxml;
    exports controllers.admin;
    opens DTO to javafx.base, org.hibernate.orm.core;
    opens controllers.server to javafx.fxml;
    exports controllers.server;
    opens controllers.login to javafx.fxml;
    exports  controllers.login;
    exports DTO;
    exports utils;
    opens utils to javafx.fxml;

}