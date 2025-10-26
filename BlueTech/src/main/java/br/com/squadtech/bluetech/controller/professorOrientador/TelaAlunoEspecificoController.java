package br.com.squadtech.bluetech.controller.professorOrientador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

public class TelaAlunoEspecificoController {

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
            nomePendente = nomeAluno; // salva para usar no initialize()
        }
    }

    @FXML
    public void initialize() {
        // Se o nome já foi definido antes do initialize(), aplica agora
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
