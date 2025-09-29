package br.com.squadtech.bluetech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Caminho absoluto dentro do resources
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/login/telaLogin.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Tela de Alunos");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
