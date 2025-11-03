package br.com.squadtech.bluetech.util;

import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/** Simple Y-axis flip utility for cards (horizontal page flip). */
public final class FlipTransitionUtil {
    private FlipTransitionUtil() {}

    public static void flip(Node node, Runnable halfwaySwap) {
        if (node == null) return;
        node.setRotationAxis(Rotate.Y_AXIS);
        RotateTransition out = new RotateTransition(Duration.millis(180), node);
        out.setFromAngle(0);
        out.setToAngle(90);
        out.setOnFinished(e -> {
            if (halfwaySwap != null) halfwaySwap.run();
            RotateTransition in = new RotateTransition(Duration.millis(180), node);
            in.setFromAngle(-90);
            in.setToAngle(0);
            in.setNode(node);
            in.setAxis(Rotate.Y_AXIS);
            in.play();
        });
        out.setAxis(Rotate.Y_AXIS);
        out.play();
    }
}
