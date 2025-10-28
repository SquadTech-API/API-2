package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class EditarSecaoAPIController implements SupportsMainController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private Button btnCancelar;
    @FXML private Button btnSalvar;
    @FXML private Button btnVoltar;

    @FXML private TextArea txtFeedbackOrientador;
    @FXML private TextArea txtMarkdownEditor;

    private PainelPrincipalController painelPrincipalController;

    private long versaoId; // ID da versão do TG atualmente carregada
    private int secaoId;   // ID da seção que está sendo editada

    // ---------------------------
    // Suporte ao MainController
    // ---------------------------
    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }

    // ---------------------------
    // Inicialização
    // ---------------------------
    @FXML
    void initialize() {
        if (txtFeedbackOrientador != null) {
            txtFeedbackOrientador.setEditable(false);
        }
        // Assertions omitidas por brevidade
    }

    // ---------------------------
    // Setar a seção e carregar a última versão (Ponto de entrada)
    // ---------------------------
    public void setSecaoId(int secaoId) {
        this.secaoId = secaoId;
        System.out.println("DEBUG [EditarSecao]: setSecaoId chamado com ID: " + secaoId);
        carregarUltimaVersao(); // Inicia a busca no banco
    }

    // ---------------------------
    // Carrega a última versão e o feedback do orientador
    // ---------------------------
    private void carregarUltimaVersao() {
        if (secaoId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Erro", "ID de seção inválido. Não foi possível carregar o conteúdo.");
            txtFeedbackOrientador.setText("ID de seção inválido.");
            return;
        }

        String sql = """
            SELECT 
                v.id AS versao_id,
                v.solucao,
                f.comentario AS feedback
            FROM TG_Versao v
            LEFT JOIN feedback f ON f.versao_id = v.id
            WHERE v.secao_id = ?
            ORDER BY v.id DESC
            LIMIT 1
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, secaoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.versaoId = rs.getLong("versao_id");
                String solucao = rs.getString("solucao");
                String comentario = rs.getString("feedback");

                final String solucaoFinal = solucao != null ? solucao : "";
                final String feedbackFinal = comentario != null && !comentario.isBlank()
                        ? comentario
                        : "Sem feedback do orientador ainda.";

                System.out.println("DEBUG [EditarSecao]: Versão ID " + this.versaoId + " carregada.");
                System.out.println("DEBUG [EditarSecao]: Conteúdo do Feedback: " + (feedbackFinal.length() > 50 ? feedbackFinal.substring(0, 50) + "..." : feedbackFinal));

                Platform.runLater(() -> {
                    txtMarkdownEditor.setText(solucaoFinal);
                    txtFeedbackOrientador.setText(feedbackFinal);
                });

            } else {
                Platform.runLater(() -> {
                    txtMarkdownEditor.setText("Nenhuma versão encontrada para esta seção. Comece a editar!");
                    txtFeedbackOrientador.setText("Nenhum feedback encontrado.");
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                txtFeedbackOrientador.setText("ERRO Crítico ao carregar dados. Verifique o console.");
                showAlert(Alert.AlertType.ERROR, "Erro de Conexão", "Falha ao buscar dados no banco de dados.");
            });
        }
    }

    // ---------------------------
    // Ações dos botões
    // ---------------------------
    @FXML
    void voltarTelaSecaoAPI(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                painelPrincipalController.loadContent("/fxml/aluno/TelaSecaoAPI.fxml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void cancelaMudancas(ActionEvent event) {
        carregarUltimaVersao();
    }

    @FXML
    void salvarNovaVersaoSessaoAPI(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Salvamento", "Lógica de salvar nova versão precisa ser implementada.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}