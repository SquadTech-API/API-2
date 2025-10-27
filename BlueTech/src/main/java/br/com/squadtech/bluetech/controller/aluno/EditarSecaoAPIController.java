package br.com.squadtech.bluetech.controller.aluno;

import br.com.squadtech.bluetech.config.ConnectionFactory;
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
        carregarFeedback();
    }

    // ---------------------------
    // Método que carrega o feedback do banco
    // ---------------------------
    private void carregarFeedback() {
        String sql = """
            SELECT comentario
            FROM feedback
            WHERE versao_id = ?
            ORDER BY created_at DESC
            LIMIT 1
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, versaoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String comentario = rs.getString("comentario");
                txtFeedbackOrientador.setText(comentario != null ? comentario : "Sem comentário.");
            } else {
                txtFeedbackOrientador.setText("Sem feedback ainda.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            txtFeedbackOrientador.setText("Erro ao carregar feedback.");
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
    }
}
