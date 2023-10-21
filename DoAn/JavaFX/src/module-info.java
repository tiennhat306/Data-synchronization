module JavaFX {
	requires javafx.base;
    requires javafx.controls;
    requires javafx.media;
    requires javafx.web;
	requires javafx.fxml;

	opens application to javafx.graphics, javafx.fxml;
}
