package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CriarSecaoAPIController implements SupportsMainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnVoltar;

    @FXML
    private TextField txtAno;

    @FXML
    private TextArea txtContribuicoes;

    @FXML
    private TextField txtEmpresa;

    @FXML
    private TextArea txtHardSkills;

    @FXML
    private TextArea txtProblema;

    @FXML
    private TextField txtRepositorio;

    @FXML
    private TextField txtSemestre;

    @FXML
    private TextField txtSemestreAno;

    @FXML
    private TextArea txtSolucao;

    @FXML
    private TextArea txtTecnologias;

    @FXML
    void salvarNovaSecaoAPI(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                String fxmlPath = "/fxml/aluno/TelaEntregasAluno.fxml";

                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                System.err.println("Falha ao carregar TelaEntregasAluno.fxml");
                e.printStackTrace();

            }
        } else {
            System.err.println("Erro: PainelPrincipalController não foi injetado em CriarSecaoAPIController.");
        }
    }

    @FXML
    void voltarEntregasAluno(ActionEvent event) {

    }

    @FXML
    void initialize() {
        assert btnSalvar != null : "fx:id=\"btnSalvar\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert btnVoltar != null : "fx:id=\"btnVoltar\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtAno != null : "fx:id=\"txtAno\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtContribuicoes != null : "fx:id=\"txtContribuicoes\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtEmpresa != null : "fx:id=\"txtEmpresa\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtHardSkills != null : "fx:id=\"txtHardSkills\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtProblema != null : "fx:id=\"txtProblema\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtRepositorio != null : "fx:id=\"txtRepositorio\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtSemestre != null : "fx:id=\"txtSemestre\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtSemestreAno != null : "fx:id=\"txtSemestreAno\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtSolucao != null : "fx:id=\"txtSolucao\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";
        assert txtTecnologias != null : "fx:id=\"txtTecnologias\" was not injected: check your FXML file 'CriarSecaoAPI.fxml'.";

    }

    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }
}
