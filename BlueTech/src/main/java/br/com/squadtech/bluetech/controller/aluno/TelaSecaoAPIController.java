package br.com.squadtech.bluetech.controller.aluno;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import br.com.squadtech.bluetech.config.ConnectionFactory; // Importe sua classe de conexão
import br.com.squadtech.bluetech.controller.SupportsMainController;
import br.com.squadtech.bluetech.controller.login.PainelPrincipalController;
import br.com.squadtech.bluetech.model.SecaoContext; // Assumindo que este é o seu SecaoContext
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class TelaSecaoAPIController implements SupportsMainController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Button btnEditarSecao;
    @FXML private Button btnEnviar;
    @FXML private Label lblStatus;
    @FXML private ListView<?> listVersoes;
    @FXML private TextArea txtChatVisualizacao;
    @FXML private TextArea txtFeedbacks;
    @FXML private TextArea txtMarkdown;
    @FXML private TextField txtMensagem;

    private PainelPrincipalController painelPrincipalController;

    // --- Métodos de UI ---

    @FXML
    void abrirEdicaoSecao(ActionEvent event) {
        if (painelPrincipalController != null) {
            try {
                // Recupera a seção atual do contexto
                Integer idSecao = SecaoContext.getIdSecaoSelecionada();
                if (idSecao == null) {
                    System.err.println("ERRO: Nenhuma seção selecionada para edição.");
                    return;
                }

                // ⚠️ AQUI ESTÁ O DETALHE: Se for para abrir a edição, você deve
                // carregar o controller manualmente para injetar o ID da VERSÃO,
                // não o ID da SEÇÃO. (Como fizemos no CriarSecaoAPIController).
                // Por simplicidade, vamos usar loadContent por agora, mas precisaria de ajuste futuro.

                String fxmlPath = "/fxml/aluno/EditarSecaoAPI.fxml";
                painelPrincipalController.loadContent(fxmlPath);

            } catch (IOException e) {
                System.err.println("Falha ao carregar EditarSecaoAPI.fxml");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void enviarMensagemOrientador(ActionEvent event) {
        // Lógica de envio de mensagem
    }

    @FXML
    void initialize() {
        // Assertions omitidas por brevidade
        carregarDadosDaSecao();
    }

    // --- Lógica de Carregamento de Dados ---

    private void carregarDadosDaSecao() {
        Integer idSecao = SecaoContext.getIdSecaoSelecionada();
        if (idSecao == null) {
            txtMarkdown.setText("Nenhuma seção selecionada.");
            txtFeedbacks.setText("Nenhuma seção selecionada.");
            return;
        }

        // 1. Busca a ID da última versão e seu conteúdo (markdown/solucao)
        // 2. Com o ID da versão, busca o feedback mais recente
        String sql = """
            SELECT 
                v.id AS versao_id, 
                v.solucao, 
                f.comentario AS feedback
            FROM TG_Versao v
            LEFT JOIN feedback f ON f.versao_id = v.id
            WHERE v.secao_id = ?
            ORDER BY v.id DESC, f.created_at DESC
            LIMIT 1
        """;

        System.out.println("DEBUG: Tentando carregar dados para Seção ID: " + idSecao);

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSecao);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String solucao = rs.getString("solucao");
                String feedback = rs.getString("feedback");

                // Garante que a atualização da UI ocorra na thread correta
                Platform.runLater(() -> {
                    txtMarkdown.setText(solucao != null ? solucao : "Nenhum conteúdo na versão.");
                    txtFeedbacks.setText(feedback != null && !feedback.isBlank()
                            ? feedback
                            : "Sem feedback do orientador ainda.");
                    txtFeedbacks.setEditable(false); // Feedbacks são apenas para leitura
                });

                System.out.println("DEBUG: Dados da última versão carregados com sucesso.");

            } else {
                Platform.runLater(() -> {
                    txtMarkdown.setText("Nenhuma versão encontrada para a seção.");
                    txtFeedbacks.setText("Nenhuma versão encontrada para a seção.");
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                txtFeedbacks.setText("ERRO ao conectar ou buscar dados do feedback.");
            });
        }
    }


    // --- Controller Support ---

    @Override
    public void setPainelPrincipalController(PainelPrincipalController controller) {
        this.painelPrincipalController = controller;
    }
}