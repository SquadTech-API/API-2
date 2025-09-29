package br.com.squadtech.bluetech.controller.aluno;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Tela_ProjetoBTController {

    @FXML
    private Button btn_aluno_visualizarArquivo;

    @FXML
    private AnchorPane buttonEnviarN;

    @FXML
    void visualizarArquivo(ActionEvent event) {
        try {
            // Carrega o FXML da próxima tela
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/aluno/TelaAluno.fxml"));
            Parent root = fxmlLoader.load();

            // Pega o Stage atual a partir do AnchorPane
            Stage stage = (Stage) buttonEnviarN.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
