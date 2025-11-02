package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Feedback;

import java.sql.*;

public class FeedbackDAO {

    /**
     * Cria a tabela feedback se não existir
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS feedback (
                id INT AUTO_INCREMENT PRIMARY KEY,
                versao_id INT NOT NULL,
                professor_id BIGINT NOT NULL,
                status ENUM('ACEITO','AJUSTES','REJEITADO') NOT NULL,
                comentario TEXT NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT fk_feedback_versao FOREIGN KEY (versao_id)
                    REFERENCES TG_Versao(Id_Versao) ON DELETE CASCADE ON UPDATE CASCADE,
                CONSTRAINT fk_feedback_professor FOREIGN KEY (professor_id)
                    REFERENCES professor(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                INDEX idx_feedback_versao (versao_id),
                INDEX idx_feedback_professor (professor_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Busca feedback existente pelo ID da versão
     */
    public Feedback buscarPorSecaoId(long versaoId) {
        String sql = "SELECT * FROM feedback WHERE versao_id = ? LIMIT 1";
        Feedback feedback = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, versaoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    feedback = new Feedback();
                    feedback.setId(rs.getInt("id"));
                    feedback.setVersaoId(rs.getInt("versao_id"));
                    feedback.setProfessorId(rs.getInt("professor_id"));
                    feedback.setStatus(rs.getString("status"));
                    feedback.setComentario(rs.getString("comentario"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) feedback.setCreatedAt(created.toLocalDateTime());
                    Timestamp updated = rs.getTimestamp("updated_at");
                    if (updated != null) feedback.setUpdatedAt(updated.toLocalDateTime());
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar feedback pela versão: " + e.getMessage(), e);
        }

        return feedback;
    }

    // Alias com nome mais preciso
    public Feedback buscarPorVersaoId(long versaoId) {
        return buscarPorSecaoId(versaoId);
    }

    /**
     * Insere ou atualiza feedback e retorna o ID do registro afetado
     */
    public Long salvarFeedbackReturnId(long versaoId, long professorId, String comentario, String status) {
        Feedback existente = buscarPorSecaoId(versaoId);

        if (existente == null) {
            String sql = "INSERT INTO feedback (versao_id, professor_id, comentario, status) VALUES (?, ?, ?, ?)";
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setLong(1, versaoId);
                ps.setLong(2, professorId);
                ps.setString(3, comentario);
                ps.setString(4, status);

                int linhas = ps.executeUpdate();
                if (linhas > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            return rs.getLong(1);
                        }
                    }
                }
                return null;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            String sql = "UPDATE feedback SET comentario = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, comentario);
                ps.setString(2, status);
                ps.setInt(3, existente.getId());

                int linhas = ps.executeUpdate();
                return linhas > 0 ? Long.valueOf(existente.getId()) : null;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Variante que também enfileira notificação ao aluno após salvar/atualizar o feedback.
     */
    public Long salvarFeedbackReturnIdAndNotify(long versaoId, long professorId, String comentario, String status) {
        Long id = salvarFeedbackReturnId(versaoId, professorId, comentario, status);
        if (id != null && id > 0) {
            NotifierEvents.onFeedbackSaved(id);
        }
        return id;
    }
}
