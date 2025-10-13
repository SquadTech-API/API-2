package br.com.squadtech.bluetech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/login/TelaLogin.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("BlueTech - Plataforma de Gestão de TGs");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
