package br.com.squadtech.bluetech.controller.professorTG;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class DashboardController {

    @FXML
    private VBox cardsBox;

    @FXML
    private Label alertLabel;

    public void initialize() {
        List<AlunoPostagem> postagens = List.of(
                new AlunoPostagem("Lívia Gomes", "Prof. Amanda Lima", 1),
                new AlunoPostagem("Thiago Menezes", "Prof. Ricardo Melo", 2),
                new AlunoPostagem("Jéssica Azevedo", "Prof. Paulo Tavares", 3),
                new AlunoPostagem("Lucas Martins", "Prof. Ana Paula", 4),
                new AlunoPostagem("Fernanda Rocha", "Prof. Geraldo Alves", 5),
                new AlunoPostagem("Mateus Viana", "Prof. Camila Souza", 6),
                new AlunoPostagem("João Pedro", "Prof. José Paulo", 7)
        );

        // Ordena do mais recente para o mais antigo
        postagens.stream()
                .sorted(Comparator.comparingInt(AlunoPostagem::dias).reversed())
                .forEach(p -> addCard(
                        p.nome(),
                        "Prof. Orientador: " + p.orientador(),
                        "Postado há: " + p.dias() + " Dias"
                ));

        // Atualiza o texto do alerta com a quantidade real de TGs
        alertLabel.setText("Você tem " + postagens.size() + " TG's que ainda não foram abertos");
    }

    private void addCard(String nome, String linha2, String linha3) {
        Label t1 = new Label(nome);
        t1.getStyleClass().add("title");
        Label t2 = new Label(linha2);
        t2.getStyleClass().add("subtitle");
        Label t3 = new Label(linha3);
        t3.getStyleClass().add("subtitle");

        VBox textBox = new VBox(4, t1, t2, t3);

        Button eye = new Button("👁");
        eye.getStyleClass().add("eye-btn");
        eye.setFocusTraversable(false);
        eye.setMouseTransparent(true); // não impede o clique no card

        HBox card = new HBox(12, textBox, eye);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("card-item");
        card.setPadding(new Insets(12));
        card.setPickOnBounds(true);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        card.setOnMouseClicked((MouseEvent event) -> {
            System.out.println("Card clicado: " + nome);
            handleCardClick();
        });

        cardsBox.getChildren().add(card);
    }

    private record AlunoPostagem(String nome, String orientador, int dias) {}

    private void handleCardClick() {
        try {
            // Carrega o FXML da tela VisualizadorTG
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/professorTG/VisualizadorTG.fxml"));
            Parent telaVisualizador = loader.load();

            Stage stage = (Stage) cardsBox.getScene().getWindow();
            stage.setScene(new Scene(telaVisualizador));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
