package br.com.squadtech.bluetech.controller.professorOrientador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

public class ControllerTelaAlunoEspecificos {

    @FXML private ToggleButton ButtonProximaTela2;
    @FXML private Label lblNomeAluno; // <- precisa existir no FXML

    private PainelPrincipalOrientadorController painelPrincipalController;
    private String nomePendente; // caso definirAluno seja chamado antes do initialize()

    public void setPainelPrincipalController(PainelPrincipalOrientadorController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    /** Recebe o nome do aluno a ser exibido. */
    public void definirAluno(String nomeAluno) {
        if (lblNomeAluno != null) {
            lblNomeAluno.setText(nomeAluno != null ? nomeAluno : "");
        } else {
            nomePendente = nomeAluno;
        }
    }

    @FXML
    public void initialize() {
        if (nomePendente != null && lblNomeAluno != null) {
            lblNomeAluno.setText(nomePendente);
            nomePendente = null;
        }
    }

    @FXML
    void passarTelaIndefinida(ActionEvent event) {
        if (painelPrincipalController != null) {
            painelPrincipalController.enviarEmail();
        }
    }
}
