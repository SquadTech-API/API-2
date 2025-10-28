package br.com.squadtech.bluetech;

import br.com.squadtech.bluetech.controller.professorTG.PainelPrincipalTGController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App1 extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Carrega o FXML do painel principal do Professor TG
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/professorTG/PainelPrincipalTG.fxml")
        );

        Parent root = fxmlLoader.load();

        // Obtém o controller e inicializa os paineis internos
        PainelPrincipalTGController controller = fxmlLoader.getController();
        controller.loadMenuTG();        // Carrega o menu lateral
        controller.loadTelaProfessorTG(); // Carrega a tela central de boas-vindas

        Scene scene = new Scene(root);
        stage.setTitle("BlueTech - Painel do Professor TG");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
