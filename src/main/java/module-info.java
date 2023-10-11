module com.datasynchronization.datasynchronization {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens demo to javafx.fxml;
    exports demo;
}