package br.com.squadtech.bluetech.controller.aluno;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class TelaAlunoController {

    @FXML
    private Button btn_estudante_enviarArquivo;

    @FXML
    void enviarArquivo(ActionEvent event) {
        try {
            // Carrega o FXML da próxima tela
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/professorOrientador/TelaAlunos.fxml"));
            Parent root = fxmlLoader.load();

            // Pega o Stage atual a partir do botão clicado
            Stage stage = (Stage) btn_estudante_enviarArquivo.getScene().getWindow();

            // Define a nova cena
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
