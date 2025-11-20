package br.com.squadtech.bluetech.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public class StageUtils {
    private static final String ICON_PATH = "/assets/icone.png";
    private static Image icon;

    public static void applyAppIcon(Stage stage) {
        if (icon == null) {
            try {
                icon = new Image(StageUtils.class.getResourceAsStream(ICON_PATH));
            } catch (Exception e) {
                System.err.println("⚠️ Não foi possível carregar o ícone: " + ICON_PATH);
                return;
            }
        }
        stage.getIcons().add(icon);
    }
}
