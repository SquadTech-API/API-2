package br.com.squadtech.bluetech.controller.aluno;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class EditarSecaoAPIController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnVoltar;

    @FXML
    private TextArea txtFeedbackOrientador;

    @FXML
    private TextArea txtMarkdownEditor;

    @FXML
    void cancelaMudancas(ActionEvent event) {

    }

    @FXML
    void salvarNovaVersaoSessaoAPI(ActionEvent event) {

    }

    @FXML
    void voltarTelaSecaoAPI(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert btnCancelar != null : "fx:id=\"btnCancelar\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";
        assert btnSalvar != null : "fx:id=\"btnSalvar\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";
        assert btnVoltar != null : "fx:id=\"btnVoltar\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";
        assert txtFeedbackOrientador != null : "fx:id=\"txtFeedbackOrientador\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";
        assert txtMarkdownEditor != null : "fx:id=\"txtMarkdownEditor\" was not injected: check your FXML file 'EditarSecaoAPI.fxml'.";

    }

}
