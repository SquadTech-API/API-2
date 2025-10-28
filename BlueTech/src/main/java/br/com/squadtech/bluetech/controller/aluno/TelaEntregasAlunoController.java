package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.model.SecaoContext;
import br.com.squadtech.bluetech.model.TGSecao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class TelaEntregasAlunoController implements SupportsMainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCriarSecao;

    @FXML
    private FlowPane flowCards;

    // Referência ao painel principal
    private PainelPrincipalController painelPrincipalController;

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    @FXML
    void CriarEntregaSessaoAPI(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                String fxmlPath = "/fxml/aluno/CriarSecaoAPI.fxml";
                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                System.err.println("Falha ao carregar CriarSecaoAPI.fxml");
                e.printStackTrace();
            }
        } else {
            System.err.println("Erro: PainelPrincipalController não foi injetado.");
        }
    }

    @FXML
    void initialize() {
        assert btnCriarSecao != null : "fx:id=\"btnCriarSecao\" not injected.";
        assert flowCards != null : "fx:id=\"flowCards\" not injected.";
        carregarCards();
    }

    private void carregarCards() {
        flowCards.getChildren().clear();
        TGSecaoDAO dao = new TGSecaoDAO();

        // Retorna lista de TGSecao
        List<TGSecao> secoes = dao.findAll(); // ou findByPortifolioId(portifolioId)

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (TGSecao s : secoes) {
            VBox card = new VBox();
            card.getStyleClass().add("card");
            card.setSpacing(5);
            card.setPadding(new Insets(10));
            card.setCursor(Cursor.HAND);

            Label titulo = new Label(String.format("%dº Semestre - %s/%s",
                    s.getApiNumero(),
                    (s.getCreatedAt() != null ? s.getCreatedAt().getYear() : "—"),
                    (s.getStatus() != null ? s.getStatus() : "—")
            ));
            titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label status = new Label("Status: " + (s.getStatus() != null ? s.getStatus() : "—"));
            String statusClass = switch (s.getStatus() == null ? "" : s.getStatus().toLowerCase()) {
                case "concluida", "aprovada" -> "status-entregue";
                case "pendente", "em andamento" -> "status-em-andamento";
                default -> "status-em-andamento";
            };
            status.getStyleClass().add(statusClass);

            String ultimaAtualizacaoTxt = (s.getUpdatedAt() != null) ? fmt.format(s.getUpdatedAt()) : "—";
            Label ultimaAtualizacao = new Label("Última atualização: " + ultimaAtualizacaoTxt);
            ultimaAtualizacao.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

            card.getChildren().addAll(titulo, status, ultimaAtualizacao);

            Long idSecaoLong = s.getId(); // guarda o id para evento
            card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> abrirTelaSecao(idSecaoLong));

            flowCards.getChildren().add(card);
        }
    }

    private void abrirTelaSecao(Long idSecaoLong) {
        if (painelPrincipalController != null) {
            try {
                // Converte Long para Integer, caso SecaoContext aceite Integer
                Integer idSecao = idSecaoLong != null ? idSecaoLong.intValue() : null;
                SecaoContext.setIdSecaoSelecionada(idSecao);

                String fxmlPath = "/fxml/aluno/TelaSecaoAPI.fxml";
                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                System.err.println("Falha ao carregar TelaSecaoAPI.fxml");
                e.printStackTrace();
            }
        }
    }
}
