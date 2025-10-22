package br.com.squadtech.bluetech.controller.professorTG;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;

import java.util.List;

public class VisualizarPortifolioTGController {

    @FXML
    private VBox cardsBox;

    @FXML
    private ComboBox<String> comboCurso;

    @FXML
    private ComboBox<String> comboSemestre;

    @FXML
    private ComboBox<String> comboPortifolio;

    @FXML
    private Button btnBuscar;

    // 🔹 Referência ao controller principal (para trocar telas)
    private PainelPrincipalTGController painelPrincipalController;

    // 🔹 Setter chamado pelo PainelPrincipalTGController
    public void setPainelPrincipalController(PainelPrincipalTGController controller) {
        this.painelPrincipalController = controller;
    }

    @FXML
    private void initialize() {
        // Inicializa combos com valores de exemplo
        comboCurso.getItems().addAll("Banco de Dados", "Análise de Sistemas");
        comboSemestre.getItems().addAll("5º Semestre", "6º Semestre");
        comboPortifolio.getItems().addAll("Portfólio 1", "Portfólio 2", "Portfólio 3");
    }

    @FXML
    private void buscarPortifolio(ActionEvent event) {
        String curso = comboCurso.getValue() != null ? comboCurso.getValue() : "";
        String semestre = comboSemestre.getValue() != null ? comboSemestre.getValue() : "";
        criarCards(semestre, curso);
    }

    public void criarCards(String semestre, String curso) {
        cardsBox.getChildren().clear();

        List<String> alunos;
        if ("Banco de Dados".equalsIgnoreCase(curso)) {
            alunos = List.of("Alice Silva", "Bruno Lima", "Carla Souza", "Diego Rocha", "Eduardo Santos");
        } else {
            alunos = List.of("Fernando Costa", "Gabriela Alves", "Henrique Pereira", "Isabela Fernandes", "João Martins");
        }

        for (int i = 0; i < alunos.size(); i++) {
            String nomeAluno = alunos.get(i);
            String infoOrientador = "Orientador: Prof. " + (i + 1);
            String infoDias = "Postado há: " + (i + 1) + " dias";

            Label t1 = new Label(nomeAluno);
            t1.getStyleClass().add("title");

            Label t2 = new Label(infoOrientador);
            t2.getStyleClass().add("subtitle");

            Label t3 = new Label(infoDias);
            t3.getStyleClass().add("subtitle");

            VBox textBox = new VBox(4, t1, t2, t3);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            Button eye = new Button("👁");
            eye.getStyleClass().add("eye-btn");
            eye.setFocusTraversable(false);

            HBox card = new HBox(12, textBox, eye);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("card-item");
            card.setPadding(new Insets(12));

            // 🔹 Quando clicar no card → abre VisualizadorTG.fxml
            card.setOnMouseClicked((MouseEvent e) -> {
                if (painelPrincipalController != null) {
                    painelPrincipalController.mostrarVisualizadorTG(nomeAluno, semestre, curso);
                } else {
                    System.out.println("Controller principal não definido!");
                }
            });

            cardsBox.getChildren().add(card);
        }
    }

    public void receberDadosAluno(String nomeAluno, String semestre, String curso) {
        System.out.println("Abrindo VisualizadorTG para: " + nomeAluno + " (" + semestre + " - " + curso + ")");
        painelPrincipalController.mostrarVisualizadorTG(nomeAluno, semestre, curso);
    }

}
