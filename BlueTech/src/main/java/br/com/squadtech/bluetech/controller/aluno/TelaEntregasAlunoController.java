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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

//Implementando a classe de Interface para chamar o Painel Principal
public class TelaEntregasAlunoController implements SupportsMainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCriarSecao;

    @FXML
    private FlowPane flowCards;

    @FXML
    void CriarEntregaSessaoAPI(ActionEvent event) {
        //Usa a referência para carregar o novo conteúdo
        if (painelPrincipalController != null) {
            try {
                String fxmlPath = "/fxml/aluno/CriarSecaoAPI.fxml";

                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                System.err.println("Falha ao carregar CriarSecaoAPI.fxml");
                e.printStackTrace();

            }
        } else {
            System.err.println("Erro: PainelPrincipalController não foi injetado em TelaEntregasAlunoController.");
        }
    }

    @FXML
    void initialize() {
        assert btnCriarSecao != null : "fx:id=\"btnCriarSecao\" was not injected: check your FXML file 'TelaEntregasAluno.fxml'.";
        assert flowCards != null : "fx:id=\"flowCards\" was not injected: check your FXML file 'TelaEntregasAluno.fxml'.";
        carregarCards();
    }

    private void carregarCards() {
        flowCards.getChildren().clear();
        TGSecaoDAO dao = new TGSecaoDAO();
        List<TGSecaoDAO.CardDados> cards = dao.listarCards();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (TGSecaoDAO.CardDados c : cards) {
            VBox card = new VBox();
            card.getStyleClass().add("card");
            card.setSpacing(5);
            card.setPadding(new Insets(10));
            card.setCursor(Cursor.HAND);

            Label titulo = new Label(String.format("%dº Semestre - %d/%s", c.apiNumero, c.ano, c.semestreAno));
            titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label status = new Label("Status: " + c.status);
            String statusClass = switch (c.status == null ? "" : c.status.toLowerCase()) {
                case "entregue", "aprovada" -> "status-entregue";
                case "em andamento", "aguardando feedback" -> "status-em-andamento";
                default -> "status-em-andamento";
            };
            status.getStyleClass().add(statusClass);
            String ultimaAtualizacaoTxt = (c.dataEnvio != null) ? fmt.format(c.dataEnvio) : "—";
            Label ultimaAtualizacao = new Label("Última atualização: " + ultimaAtualizacaoTxt);
            ultimaAtualizacao.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

            card.getChildren().addAll(titulo, status, ultimaAtualizacao);

            card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> abrirTelaSecao(c.idSecao));
            flowCards.getChildren().add(card);
        }
    }

    private void abrirTelaSecao(int idSecao) {
        if (painelPrincipalController != null) {
            try {
                SecaoContext.setIdSecaoSelecionada(idSecao);
                String fxmlPath = "/fxml/aluno/TelaSecaoAPI.fxml";
                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                System.err.println("Falha ao carregar TelaSecaoAPI.fxml");
                e.printStackTrace();
            }
        }
    }

    //Variável para guardar a referência
    private PainelPrincipalController painelPrincipalController;

    //Método da Interface para injetar a referência
    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }
}
