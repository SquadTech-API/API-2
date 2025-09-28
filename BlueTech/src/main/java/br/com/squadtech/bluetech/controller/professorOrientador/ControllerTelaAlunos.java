package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ControllerTelaAlunos {


    @FXML
    private Button ButtonPesquisar;

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
    void PassarTela(MouseEvent event) {

    }

}
