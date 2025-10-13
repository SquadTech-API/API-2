package br.com.squadtech.bluetech.controller.professorTG;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.io.IOException;

public class VisualizadorTGController {

    @FXML
    private Button btn_professorTG_finalizar;

    @FXML
    private CheckBox cbx_professorTG_VisuAluno;

    // Método chamado ao clicar no botão "Finalizar"
    @FXML
    void finalizar(ActionEvent event) {
        try {
            // Carrega o FXML da tela de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/TelaLogin.fxml"));
            Parent root = loader.load();

            // Pega o Stage atual a partir do botão clicado
            Stage stage = (Stage) btn_professorTG_finalizar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método chamado quando o checkbox é clicado
    @FXML
    void handleCheckboxAction(ActionEvent event) {
        if (cbx_professorTG_VisuAluno.isSelected()) {
            System.out.println("O CheckBox está selecionado.");
        } else {
            System.out.println("O CheckBox foi desmarcado.");
        }
    }
}
