package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {

    // Cria a tabela caso não exista
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS feedback (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                versao_id BIGINT NOT NULL,
                professor_id BIGINT NOT NULL,
                status ENUM('ACEITO','AJUSTES','REJEITADO') NOT NULL,
                comentario TEXT NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT fk_feedback_versao FOREIGN KEY (versao_id)
                    REFERENCES tg_versao(id) ON DELETE CASCADE ON UPDATE CASCADE,
                CONSTRAINT fk_feedback_professor FOREIGN KEY (professor_id)
                    REFERENCES professor(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                INDEX idx_feedback_versao (versao_id),
                INDEX idx_feedback_professor (professor_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela feedback: " + e.getMessage(), e);
        }
    }

    // Inserir um novo feedback
    public void insert(Feedback feedback) {
        String sql = "INSERT INTO feedback (versao_id, professor_id, status, comentario) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, feedback.getVersaoId());
            ps.setInt(2, feedback.getProfessorId());
            ps.setString(3, feedback.getStatus());
            ps.setString(4, feedback.getComentario());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    feedback.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir feedback: " + e.getMessage(), e);
        }
    }

    // Atualizar feedback
    public void update(Feedback feedback) {
        String sql = "UPDATE feedback SET status = ?, comentario = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, feedback.getStatus());
            ps.setString(2, feedback.getComentario());
            ps.setInt(3, feedback.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar feedback: " + e.getMessage(), e);
        }
    }

    // Listar todos os feedbacks de uma versão
    public List<Feedback> listarPorVersao(int versaoId) {
        String sql = "SELECT * FROM feedback WHERE versao_id = ?";
        List<Feedback> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, versaoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Feedback f = new Feedback();
                    f.setId(rs.getInt("id"));
                    f.setVersaoId(rs.getInt("versao_id"));
                    f.setProfessorId(rs.getInt("professor_id"));
                    f.setStatus(rs.getString("status"));
                    f.setComentario(rs.getString("comentario"));
                    f.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    f.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    lista.add(f);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar feedbacks: " + e.getMessage(), e);
        }

        return lista;
    }

    // Buscar um feedback específico
    public Feedback buscarPorId(int id) {
        String sql = "SELECT * FROM feedback WHERE id = ?";
        Feedback f = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    f = new Feedback();
                    f.setId(rs.getInt("id"));
                    f.setVersaoId(rs.getInt("versao_id"));
                    f.setProfessorId(rs.getInt("professor_id"));
                    f.setStatus(rs.getString("status"));
                    f.setComentario(rs.getString("comentario"));
                    f.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    f.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar feedback: " + e.getMessage(), e);
        }

        return f;
    }
}
