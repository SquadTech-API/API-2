package br.com.squadtech.bluetech.controller.professorTG;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MenuProfessorTGController {

    @FXML
    private Label lblTituloProfessorTG;

    @FXML
    private ImageView imgViewFotoProfessorTG;

    @FXML
    private Label lblProfessorTG;

    @FXML
    private Label lblSemestreTG;

    @FXML
    private VBox vboxMenuProfessorTG;

    // ✅ Adicionado: referência ao painel principal
    private PainelPrincipalTGController painelPrincipalController;

    // ✅ Adicionado: setter chamado pelo PainelPrincipalTGController
    public void setPainelPrincipalController(PainelPrincipalTGController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    @FXML
    private void abrirAgendamentosTG() {
        System.out.println("Abrindo Agendamentos de TG...");
    }

    @FXML
    private void abrirCadastrarOrientadores() {
        System.out.println("Abrindo Cadastrar Orientadores...");
    }

    @FXML
    private void abrirListaAlunos() {
        System.out.println("Abrindo Lista de Alunos...");
    }

    @FXML
    private void selecionarBancoDados5() {
        if (painelPrincipalController != null)
            painelPrincipalController.mostrarVisualizarPortifolio("5º Semestre", "Banco de Dados");
    }

    @FXML
    private void selecionarAnaliseSistemas5() {
        if (painelPrincipalController != null)
            painelPrincipalController.mostrarVisualizarPortifolio("5º Semestre", "Análise de Sistemas");
    }

    @FXML
    private void selecionarBancoDados6() {
        if (painelPrincipalController != null)
            painelPrincipalController.mostrarVisualizarPortifolio("6º Semestre", "Banco de Dados");
    }

    @FXML
    private void selecionarAnaliseSistemas6() {
        if (painelPrincipalController != null)
            painelPrincipalController.mostrarVisualizarPortifolio("6º Semestre", "Análise de Sistemas");
    }

    @FXML
    private void initialize() {
        lblTituloProfessorTG.setText("Painel do Professor TG");
        lblProfessorTG.setText("PROFESSOR TG: Emanuel Mineda");
        lblSemestreTG.setText("SEMESTRE RESPONSÁVEL: 6º Semestre");
    }
}
