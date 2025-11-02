package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.dao.TGSecaoDAO;
import br.com.squadtech.bluetech.model.SessaoUsuario;
import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.model.SecaoContext;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Implementando a classe de Interface para chamar o Painel Principal
public class TelaEntregasAlunoController implements SupportsMainController {

    private static final Logger log = LoggerFactory.getLogger(TelaEntregasAlunoController.class);

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
                log.error("Falha ao carregar CriarSecaoAPI.fxml", e);
            }
        } else {
            log.error("PainelPrincipalController não foi injetado em TelaEntregasAlunoController.");
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
        Usuario user = SessaoUsuario.getUsuarioLogado();
        if (user == null || user.getEmail() == null) {
            return; // sem sessão válida
        }
        List<TGSecaoDAO.CardDados> cards = dao.listarCards(user.getEmail());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (TGSecaoDAO.CardDados c : cards) {
            VBox card = new VBox();
            card.getStyleClass().add("card");
            card.setSpacing(8);
            card.setPadding(new Insets(10));
            card.setCursor(Cursor.HAND);
// Header com título e botão excluir
            HBox header = new HBox();
            header.setSpacing(8);
            String semCurso = (c.semestreCurso != null && !c.semestreCurso.isBlank()) ? c.semestreCurso : "—";
            String semAno = (c.semestreAno != null && !c.semestreAno.isBlank()) ? c.semestreAno : "—";
            Label titulo = new Label(String.format("%s -%d-%s", semCurso, c.ano, semAno));
            titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
//Botão de exclusão com ícone FontAwesome
            MaterialIconView trashIcon = new MaterialIconView(MaterialIcon.DELETE);
            trashIcon.setGlyphSize(14);
            trashIcon.setFill(javafx.scene.paint.Color.valueOf("#c0392b"));
            Button btnExcluir = new Button();
            btnExcluir.setGraphic(trashIcon);
            btnExcluir.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            btnExcluir.setOnAction(e -> confirmarExclusao(c.idSecao));
            header.getChildren().addAll(titulo, spacer, btnExcluir);
//Destaque com número da API
            Label apiLabel = new Label("API " + c.apiNumero);
            apiLabel.setStyle("-fx-background-color: #eef5ff; -fx-text-fill: #2c3e50; -fx-padding: 2 6; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-weight: bold;");
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
            card.getChildren().addAll(header, apiLabel, status, ultimaAtualizacao);
            card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> abrirTelaSecao(c.idSecao));
            flowCards.getChildren().add(card);
        }
    }

    private void confirmarExclusao(int idSecao) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Excluir Seção");
        confirm.setHeaderText("Confirma a exclusão deste card?");
        confirm.setContentText("Esta ação não poderá ser desfeita.");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                TGSecaoDAO dao = new TGSecaoDAO();
                Usuario user = SessaoUsuario.getUsuarioLogado();
                if (user == null || user.getEmail() == null) return;
                dao.deleteByIdAndEmail(idSecao, user.getEmail());
                carregarCards();
            }
        });
    }

    private void abrirTelaSecao(int idSecao) {
        if (painelPrincipalController != null) {
            try {
                SecaoContext.setIdSecaoSelecionada(idSecao);
                String fxmlPath = "/fxml/aluno/TelaSecaoAPI.fxml";
                painelPrincipalController.loadContent(fxmlPath);
            } catch (IOException e) {
                log.error("Falha ao carregar TelaSecaoAPI.fxml", e);
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
