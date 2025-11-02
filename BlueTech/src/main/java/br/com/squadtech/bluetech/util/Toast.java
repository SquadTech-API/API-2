package br.com.squadtech.bluetech.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/** Simple non-blocking toast for JavaFX scenes. */
public final class Toast {
    private Toast() {}

    public static void show(Scene scene, String message) {
        if (scene == null || scene.getRoot() == null || message == null || message.isBlank()) return;

        Label label = new Label(message);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        label.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-background-radius: 8; -fx-padding: 10 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,.4), 8, 0, 0, 2);");

        StackPane root = (StackPane) scene.getRoot();
        StackPane overlay;
        if (root instanceof StackPane sp) {
            overlay = sp;
        } else {
            overlay = new StackPane();
            overlay.getChildren().add(root);
            scene.setRoot(overlay);
        }

        StackPane container = new StackPane(label);
        StackPane.setAlignment(container, Pos.BOTTOM_CENTER);
        StackPane.setMargin(container, new Insets(0, 0, 24, 0));

        container.setOpacity(0.0);
        overlay.getChildren().add(container);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        PauseTransition stay = new PauseTransition(Duration.seconds(2.2));
        stay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), container);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> overlay.getChildren().remove(container));
            fadeOut.play();
        });
        stay.play();
    }
}

