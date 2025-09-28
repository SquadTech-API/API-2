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
    void passarTelaIndefinida(ActionEvent event) throws IOException {
        // o metodo abaixo é onde passa para proxima tela, vc só precisa por pra qual tela vai ali entre as aspas
        Parent root = FXMLLoader.load(getClass().getResource(""));
        Scene scene = new Scene(root);

        // pega a janela atual
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}



