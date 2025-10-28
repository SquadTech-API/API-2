package br.com.squadtech.bluetech.controller.professorOrientador;

import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.model.TGSecao;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TelaAlunoEspecificoController {

    @FXML private Label lblNomeAluno;
    @FXML private ToggleButton tgS1, tgS2, tgS3, tgS4, tgS5, tgS6;
    @FXML private ScrollPane spSessao1, spSessao2, spSessao3, spSessao4, spSessao5, spSessao6;
    @FXML private VBox VBoxSessao1, VBoxSessao2, VBoxSessao3, VBoxSessao4, VBoxSessao5, VBoxSessao6;

    private long alunoId;
    private final TGSecaoDAO tgSecaoDAO = new TGSecaoDAO();
    private PainelPrincipalOrientadorController painelPrincipalController;

    public void setPainelPrincipalController(PainelPrincipalOrientadorController painelPrincipalController) {
        this.painelPrincipalController = painelPrincipalController;
    }

    public void setAlunoId(long alunoId, String nomeAluno) {
        this.alunoId = alunoId;
        lblNomeAluno.setText(nomeAluno);
        carregarSecoesDoAluno();
        configurarToggleButtons();
    }

    /**
     * Carrega todas as seções do aluno direto em código (sem FXML de card)
     */
    private void carregarSecoesDoAluno() {
        List<TGSecao> secoes = tgSecaoDAO.findByAlunoId(alunoId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        VBox[] vboxes = {VBoxSessao1, VBoxSessao2, VBoxSessao3, VBoxSessao4, VBoxSessao5, VBoxSessao6};
        for (VBox vbox : vboxes) vbox.getChildren().clear();

        for (TGSecao secao : secoes) {
            HBox card = new HBox(10);
            card.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: #f9f9f9;");

            Label lblTitulo = new Label("API " + secao.getApiNumero());
            Label lblStatus = new Label(secao.getStatus().equalsIgnoreCase("CONCLUIDA") ? "Concluída" : "Pendente");
            lblStatus.setStyle(secao.getStatus().equalsIgnoreCase("CONCLUIDA") ? "-fx-text-fill: green;" : "-fx-text-fill: red;");

            Label lblData = new Label(secao.getDataValidacao() != null
                    ? "Última validação: " + secao.getDataValidacao().format(formatter)
                    : "Sem validação ainda");

            card.getChildren().addAll(lblTitulo, lblStatus, lblData);
            card.setOnMouseClicked(e -> abrirTelaFeedback(secao.getId()));

            vboxes[secao.getApiNumero() - 1].getChildren().add(card);
        }
    }

    /**
     * Configura os ToggleButtons para mostrar a sessão correta
     */
    private void configurarToggleButtons() {
        ToggleButton[] toggles = {tgS1, tgS2, tgS3, tgS4, tgS5, tgS6};
        ScrollPane[] scrollers = {spSessao1, spSessao2, spSessao3, spSessao4, spSessao5, spSessao6};

        for (int i = 0; i < toggles.length; i++) {
            int index = i;
            toggles[i].setOnAction(e -> {
                for (int j = 0; j < scrollers.length; j++) {
                    scrollers[j].setVisible(j == index);
                    toggles[j].setSelected(j == index);
                }
            });
        }

        // Inicialmente mostra apenas a primeira sessão
        for (int j = 0; j < scrollers.length; j++) {
            scrollers[j].setVisible(j == 0);
            toggles[j].setSelected(j == 0);
        }
    }

    /**
     * Abre a tela de feedback para a seção
     */
    private void abrirTelaFeedback(long secaoId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/fxml/professorOrientador/TelaFeedback.fxml"));
            Parent root = loader.load();

            TelaFeedbackController controller = loader.getController();
            controller.setSecaoId(secaoId);

            Stage stage = new Stage();
            stage.setTitle("Feedback da Seção");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
