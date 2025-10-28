package br.com.squadtech.bluetech.controller.professorTG;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class VisualizadorTGController {

    @FXML
    private CheckBox cbx_professorTG_VisuAluno;

    @FXML
    private Label lblNomeAluno; // Aluno
    @FXML
    private Label lblSemestre; // Se quiser exibir semestre (adicione no FXML depois)
    @FXML
    private Label lblCurso; // Se quiser exibir curso (adicione no FXML depois)

    private String nomeAluno;
    private String semestre;
    private String curso;

    @FXML
    public void initialize() {
        // Inicialização segura
    }

    /**
     * Esse método é chamado pelo PainelPrincipalTGController
     * para preencher as informações do aluno ao abrir esta tela.
     */
    public void receberDadosAluno(String nomeAluno, String semestre, String curso) {
        this.nomeAluno = nomeAluno;
        this.semestre = semestre;
        this.curso = curso;

        // Atualiza a label com segurança (se existir)
        if (lblNomeAluno != null) lblNomeAluno.setText("Aluno: " + nomeAluno);
        if (lblSemestre != null) lblSemestre.setText("Semestre: " + semestre);
        if (lblCurso != null) lblCurso.setText("Curso: " + curso);
    }

    @FXML
    private void handleCheckboxAction() {
        System.out.println("Checkbox clicado: " + cbx_professorTG_VisuAluno.isSelected());
    }

    @FXML
    private void finalizar() {
        System.out.println("Finalizar clicado pelo professor.");
    }

    /**
     * Caso você queira que clicar no card também faça algo,
     * esse método pode ser ligado no FXML com onMouseClicked="#abrirTG"
     */
    @FXML
    private void abrirTG(MouseEvent event) {
        System.out.println("Abrir TG clicado por: " + nomeAluno);
    }
}
