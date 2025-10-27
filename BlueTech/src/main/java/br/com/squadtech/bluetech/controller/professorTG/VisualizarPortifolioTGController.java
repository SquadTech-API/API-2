package br.com.squadtech.bluetech.controller.professorTG;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import javafx.event.ActionEvent;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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

    private PainelPrincipalTGController painelPrincipalController;

    public void setPainelPrincipalController(PainelPrincipalTGController controller) {
        this.painelPrincipalController = controller;
    }

    @FXML
    private void initialize() {
        comboCurso.getItems().addAll("Banco de Dados", "Análise de Sistemas");
        comboSemestre.getItems().addAll("5º Semestre", "6º Semestre");
    }

    @FXML
    private void buscarPortifolio(ActionEvent event) {
        String curso = comboCurso.getValue() != null ? comboCurso.getValue() : "";
        String semestre = comboSemestre.getValue() != null ? comboSemestre.getValue() : "";
        criarCards(semestre, curso);
    }

    public void criarCards(String semestre, String curso) {
        cardsBox.getChildren().clear();

        // Query para trazer todos os alunos e professores que os orientam
        String sql = """
            SELECT 
                pa.id AS aluno_id,
                u.nome AS aluno_nome,
                GROUP_CONCAT(u2.nome SEPARATOR ', ') AS professores,
                tp.status AS portifolio_status
            FROM perfil_aluno pa
            JOIN usuario u ON pa.usuario_email = u.email
            LEFT JOIN orienta o ON o.aluno_id = pa.id AND o.ativo = TRUE
            LEFT JOIN professor pr ON pr.id = o.professor_id
            LEFT JOIN usuario u2 ON pr.usuario_email = u2.email
            LEFT JOIN tg_portifolio tp ON tp.aluno_id = pa.id
            GROUP BY pa.id, u.nome, tp.status
            ORDER BY u.nome
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String nomeAluno = rs.getString("aluno_nome");
                String professores = rs.getString("professores") != null ? rs.getString("professores") : "Sem professor";
                String statusPortifolio = rs.getString("portifolio_status") != null ? rs.getString("portifolio_status") : "Sem portfólio";

                // Labels
                Label t1 = new Label(nomeAluno);
                t1.getStyleClass().add("title");

                Label t2 = new Label("Orientador(es): " + professores);
                t2.getStyleClass().add("subtitle");

                Label t3 = new Label("Status: " + statusPortifolio);
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

                // Ao clicar no card, abre visualizador
                card.setOnMouseClicked((MouseEvent e) -> {
                    if (painelPrincipalController != null) {
                        painelPrincipalController.mostrarVisualizadorTG(nomeAluno, semestre, curso);
                    }
                });

                cardsBox.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
