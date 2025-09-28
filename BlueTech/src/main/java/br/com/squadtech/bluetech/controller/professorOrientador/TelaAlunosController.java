package br.com.squadtech.bluetech.controller.professorOrientador;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class TelaAlunosController {

    @FXML
    private Button ButtonPesquisar;

    @FXML
    private ToggleButton ButtonProximaTela;

    @FXML
    private HBox HboxAluno;

    @FXML
    private TextField TextFieldNomePesquisa;

    @FXML
    private ToggleButton ToggleButonTodosAlunos;

    @FXML
    private ToggleButton ToggleButtonCorrigidos;

    @FXML
    private ToggleButton ToggleButtonNaoCorrigidos;

    @FXML
    private VBox VBoxListaAlunos;

    @FXML
    void passarTelaAlunoEspecifico(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/professorOrientador/telaAlunoEspecifico.fxml"));
        Scene scene = new Scene(root);

        // pega a janela atual
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}