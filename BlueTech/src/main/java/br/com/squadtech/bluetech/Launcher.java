package br.com.squadtech.bluetech;

import br.com.squadtech.bluetech.config.DatabaseInitializer;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        //Bootstrap do banco de dados antes de iniciar a aplicação JavaFX
        DatabaseInitializer.init();
        Application.launch(App.class, args);
    }
}