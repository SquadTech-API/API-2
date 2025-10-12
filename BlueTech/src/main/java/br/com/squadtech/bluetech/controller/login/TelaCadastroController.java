package br.com.squadtech.bluetech.controller.login;

import com.jfoenix.controls.JFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class TelaCadastroController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXButton btnSignAlunoConf;

    @FXML
    private ChoiceBox<?> cBoxSignAlunoCurso;

    @FXML
    private ChoiceBox<?> cBoxSignAlunoOrient;

    @FXML
    private AnchorPane paneSignCadastro;

    @FXML
    private PasswordField passFldSignAluno;

    @FXML
    private PasswordField passFldSignAlunoConf;

    @FXML
    private TextField txtFldSignAlunoEmail;

    @FXML
    private TextField txtFldSignAlunoNome;

    @FXML
    private TextField txtFldSignAlunoRa;

    @FXML
    void initialize() {
        assert btnSignAlunoConf != null : "fx:id=\"btnSignAlunoConf\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert cBoxSignAlunoCurso != null : "fx:id=\"cBoxSignAlunoCurso\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert cBoxSignAlunoOrient != null : "fx:id=\"cBoxSignAlunoOrient\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert paneSignCadastro != null : "fx:id=\"paneSignCadastro\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert passFldSignAluno != null : "fx:id=\"passFldSignAluno\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert passFldSignAlunoConf != null : "fx:id=\"passFldSignAlunoConf\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert txtFldSignAlunoEmail != null : "fx:id=\"txtFldSignAlunoEmail\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert txtFldSignAlunoNome != null : "fx:id=\"txtFldSignAlunoNome\" was not injected: check your FXML file 'TelaCadastro.fxml'.";
        assert txtFldSignAlunoRa != null : "fx:id=\"txtFldSignAlunoRa\" was not injected: check your FXML file 'TelaCadastro.fxml'.";

    }

}
