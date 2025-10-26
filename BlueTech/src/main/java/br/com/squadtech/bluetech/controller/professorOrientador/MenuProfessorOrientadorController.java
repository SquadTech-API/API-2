package br.com.squadtech.bluetech.controller.professorOrientador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class MenuProfessorOrientadorController {

    @FXML private Label lblTituloProfessorOri;
    @FXML private ImageView imgViewFotoProfessorOri;
    @FXML private Label lblProfessorOri;
    @FXML private Label lblSemestreOri;

    private PainelPrincipalOrientadorController painelPrincipalController;

    public void setPainelPrincipalController(PainelPrincipalOrientadorController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @FXML
    private void initialize() {
        lblProfessorOri.setText("PROFESSOR ORIENTADOR: Emanuel Mineda");
        lblSemestreOri.setText("SEMESTRE RESPONSÁVEL: 6º Semestre");
    }

    @FXML
    private void abrirListaAlunos() {
        if (painelPrincipalController != null) {
            painelPrincipalController.mostrarTelaAlunos();
        }
    }
    @FXML
    private void enviarEmail() {
        if (painelPrincipalController != null) {
            painelPrincipalController.enviarEmail();
        }
    }
}
