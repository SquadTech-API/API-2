package br.com.squadtech.bluetech.controller.professorOrientador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerTelaAlunoEspecificos {

    @FXML
    private ToggleButton ButtonProximaTela2;

    @FXML
    void passarTelaIndefinida(ActionEvent event) {
        try {
            // Carrega o FXML da tela Dashboard
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/professorTG/dashboard.fxml"));
            Scene scene = new Scene(root);

            // Pega a janela atual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receberAluno(Integer idAluno, String nomeAluno) {
        // Aqui você pode guardar esses dados para usar na tela
        System.out.println("Aluno: " + nomeAluno + " | ID: " + idAluno);
    }
}
