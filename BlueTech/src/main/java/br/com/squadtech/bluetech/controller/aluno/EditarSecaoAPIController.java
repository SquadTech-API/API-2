package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import javafx.application.Platform; // Adicionado para boas práticas
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class EditarSecaoAPIController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private Button btnCancelar;
    @FXML private Button btnSalvar;
    @FXML private Button btnVoltar;

    @FXML private TextArea txtFeedbackOrientador;
    @FXML private TextArea txtMarkdownEditor;

    private long versaoId; // ID da versão do TG a ser carregada

    // ---------------------------
    // Setar a versão e carregar feedback
    // ---------------------------
    public void setVersaoId(long versaoId) {
        this.versaoId = versaoId;
        // 🚨 DEBUG: Confirma o ID que chegou
        System.out.println("DEBUG: setVersaoId chamado com ID: " + versaoId);

        // Chamamos a lógica de carregamento de dados
        carregarFeedback();
    }

    // ---------------------------
    // Método que carrega o feedback do banco
    // ---------------------------
    private void carregarFeedback() {
        if (this.versaoId <= 0) {
            txtFeedbackOrientador.setText("ID de versão inválido para carregar feedback.");
            System.err.println("ERRO: Tentativa de carregar feedback com versaoId <= 0. ID: " + this.versaoId);
            return;
        }

        String sql = """
            SELECT comentario
            FROM feedback
            WHERE versao_id = ?
            ORDER BY created_at DESC
            LIMIT 1
        """;

        String comentario = null;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, versaoId);
            System.out.println("DEBUG: Executando query para versao_id: " + versaoId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                comentario = rs.getString("comentario");
                System.out.println("DEBUG: Feedback encontrado: " + comentario);
            } else {
                System.out.println("DEBUG: NENHUM feedback encontrado para versao_id: " + versaoId);
            }

            // É essencial atualizar componentes de UI na JavaFX Application Thread
            String textoFinal = comentario != null ? comentario : "Sem feedback ainda.";

            Platform.runLater(() -> {
                txtFeedbackOrientador.setText(textoFinal);
                txtFeedbackOrientador.setEditable(false);
            });


        } catch (Exception e) {
            // Se o erro ocorrer na Thread de UI ou na Thread de banco (se fosse assíncrono),
            // este print é vital.
            e.printStackTrace();

            Platform.runLater(() -> {
                txtFeedbackOrientador.setText("Erro ao carregar feedback. Verifique o console para detalhes.");
                txtFeedbackOrientador.setEditable(false);
            });
        }
    }

    // ---------------------------
    // Ações dos botões (opcional)
    // ---------------------------
    @FXML
    void cancelaMudancas(ActionEvent event) { /* opcional */ }

    @FXML
    void salvarNovaVersaoSessaoAPI(ActionEvent event) { /* opcional */ }

    @FXML
    void voltarTelaSecaoAPI(ActionEvent event) { /* opcional */ }

    @FXML
    void initialize() {
        assert btnCancelar != null;
        assert btnSalvar != null;
        assert btnVoltar != null;
        assert txtFeedbackOrientador != null;
        assert txtMarkdownEditor != null;

        // Se a tela for carregada diretamente sem um ID (o que não deve acontecer no seu fluxo)
        // você pode adicionar um log aqui.
        // Platform.runLater(this::carregarFeedback); // Removido, pois setVersaoId() é quem deve iniciar a carga.
    }
}