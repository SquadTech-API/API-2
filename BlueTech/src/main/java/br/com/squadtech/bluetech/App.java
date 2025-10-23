package br.com.squadtech.bluetech;

import br.com.squadtech.bluetech.dao.ConnectionFactory;
import br.com.squadtech.bluetech.dao.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/login/TelaLogin.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load());
        String iconPath = "/images/icone.png";

        try (InputStream iconStream = getClass().getResourceAsStream(iconPath)) {
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                stage.getIcons().add(icon);
            } else {
                System.err.println("Aviso: Ícone da aplicação não encontrado em: " + iconPath);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone da aplicação.");
            e.printStackTrace();
        }
        stage.setTitle("BlueTech - Plataforma de Gestão de TGs");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        ConnectionFactory.closePool();
    }

    public static void main(String[] args) {
        DatabaseInitializer.init(); //Bootstrap do banco de dados para iniciar com a aplicação
        launch();
    }


}
