package common.viewattribute;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {
    private static final int COUNTDOWN_TIME = 3;

    public static void showToast(Stage parentStage, int status,  String message) {
        Popup popup = new Popup();

        popup.setOpacity(0.5);
        popup.setAutoHide(true);
        popup.setOnCloseRequest(event -> {
            popup.hide();
        });

        Label title = new Label(status == 1 ? "Thành công" : "Thất bại");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label messageLbl = new Label(message);
        messageLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");

        ProgressBar progressBar = new ProgressBar();

        progressBar.setStyle("-fx-accent: #000000; -fx-padding: 5px 0 0 0;");
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(15);
        progressBar.setProgress(1);

        Timeline countdown = new Timeline(
                new KeyFrame(Duration.seconds(0.01), e -> {
                    double progress = progressBar.getProgress();
                    if(progress > 0) {
                        progressBar.setProgress(progress - 0.01/COUNTDOWN_TIME);
                    }
                })
        );

        countdown.setCycleCount(COUNTDOWN_TIME * 100);
        countdown.setOnFinished(event -> {
            popup.hide();
        });
        countdown.play();

        Button closeBtn = new Button("X");
        closeBtn.setOnAction(event -> {
            countdown.stop();
            popup.hide();
        });

        FontAwesomeIconView successIcon = new FontAwesomeIconView();
        successIcon.setGlyphName("CHECK");
        successIcon.setSize("50");
        FontAwesomeIconView errorIcon = new FontAwesomeIconView();
        errorIcon.setGlyphName("TIMES");
        errorIcon.setSize("50");
        Label icon = new Label();
        icon.setGraphic(status == 1 ? successIcon : errorIcon);


        HBox titleBar = new HBox(closeBtn);
        titleBar.setAlignment(Pos.CENTER_RIGHT);

        VBox center = new VBox(titleBar, title, messageLbl);
        center.setAlignment(Pos.CENTER_LEFT);
        center.setSpacing(5);
        center.setPrefWidth(220);
        center.setMaxWidth(220);
        center.setPadding(new javafx.geometry.Insets(0, 0, 0, 10));

        HBox grid = new HBox(icon, center);
        grid.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(grid, progressBar);
        content.setStyle("-fx-background-color: " + (status == 1 ? "rgba(16,196,105,.8)" : "rgba(255,91,91,.8)") + "; -fx-padding: 5px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        content.setMaxWidth(300);
        content.setMaxHeight(150);

        popup.getContent().add(content);
        popup.show(parentStage, parentStage.getX() + parentStage.getWidth() - content.getMaxWidth() - 20, parentStage.getY() + parentStage.getHeight() - content.getMaxHeight() - 20);
    }
}
