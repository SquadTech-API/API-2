package br.com.squadtech.bluetech.controller.professorTG;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DetalhesAlunoController {

    @FXML
    private Label nomeAluno;
    @FXML
    private Label profOrientador;
    @FXML
    private Label postagemData;

    // Método para configurar as informações do aluno
    public void setAluno(String nome, String prof, String data) {
        nomeAluno.setText(nome);
        profOrientador.setText("Prof. Orientador: " + prof);
        postagemData.setText(data);
    }
}
