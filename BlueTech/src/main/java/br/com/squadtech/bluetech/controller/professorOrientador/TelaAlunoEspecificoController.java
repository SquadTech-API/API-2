package br.com.squadtech.bluetech.controller.professorOrientador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class TelaAlunoEspecificoController {

    @FXML private Label lblNomeAluno;

    @FXML private ToggleButton tgS1, tgS2, tgS3, tgS4, tgS5, tgS6;

    @FXML private VBox VBoxSessao1;
    @FXML private VBox VBoxSessao2;
    @FXML private VBox VBoxSessao3;
    @FXML private VBox VBoxSessao4;
    @FXML private VBox VBoxSessao5;
    @FXML private VBox VBoxSessao6;

    private String nomePendente;

    @FXML
    public void initialize() {
        // Aplica nome pendente se definido antes do initialize()
        if (nomePendente != null && lblNomeAluno != null) {
            lblNomeAluno.setText(nomePendente);
            nomePendente = null;
        }

        // Carrega cards de exemplo na sessão 1
        carregarCardsExemplo(VBoxSessao1);
    }

    /** Define o nome do aluno a ser exibido */
    public void definirAluno(String nomeAluno) {
        if (lblNomeAluno != null) {
            lblNomeAluno.setText(nomeAluno != null ? nomeAluno : "");
        } else {
            nomePendente = nomeAluno;
        }
    }

    /** Troca a sessão visível de acordo com o toggle selecionado */
    @FXML
    private void trocarSessao1() { mostrarSessao(1); }
    @FXML
    private void trocarSessao2() { mostrarSessao(2); }
    @FXML
    private void trocarSessao3() { mostrarSessao(3); }
    @FXML
    private void trocarSessao4() { mostrarSessao(4); }
    @FXML
    private void trocarSessao5() { mostrarSessao(5); }
    @FXML
    private void trocarSessao6() { mostrarSessao(6); }

    private void mostrarSessao(int sessao) {
        VBoxSessao1.setVisible(sessao == 1);
        VBoxSessao2.setVisible(sessao == 2);
        VBoxSessao3.setVisible(sessao == 3);
        VBoxSessao4.setVisible(sessao == 4);
        VBoxSessao5.setVisible(sessao == 5);
        VBoxSessao6.setVisible(sessao == 6);
    }

    /** Cria cards dinâmicos de exemplo dentro da VBox informada */
    private void carregarCardsExemplo(VBox container) {
        // Simulando dados de portfólios
        List<String[]> entregas = List.of(
                new String[]{"EntregaTg1.md", "há 14 dias", "excla.png"},
                new String[]{"EntregaTg2.md", "22/08/25", "check.png"},
                new String[]{"EntregaTg3.md", "há 3 dias", "excla.png"}
        );

        for (String[] e : entregas) {
            container.getChildren().add(criarCard(e[0], e[1], e[2]));
        }
    }

    /** Cria um card HBox com arquivo, data/status e evento de clique */
    private HBox criarCard(String nomeArquivo, String data, String statusImagem) {
        HBox card = new HBox(20);
        card.setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label lblArquivo = new Label(nomeArquivo);
        lblArquivo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblData = new Label(data);
        lblData.setStyle("-fx-font-size: 14px;");

        ImageView status = new ImageView(new Image(getClass().getResourceAsStream("/../../images/" + statusImagem)));
        status.setFitHeight(24);
        status.setFitWidth(24);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(lblArquivo, spacer, lblData, status);

        // Evento ao clicar no card
        card.setOnMouseClicked((MouseEvent e) -> {
            System.out.println("Card clicado: " + nomeArquivo);
            // Aqui você pode mostrar detalhes do arquivo ou marcar entrega
        });

        return card;
    }
}
