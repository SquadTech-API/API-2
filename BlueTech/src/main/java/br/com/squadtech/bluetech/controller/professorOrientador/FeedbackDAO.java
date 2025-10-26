package br.com.squadtech.bluetech.controller.professorOrientador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FeedbackDAO {

    /**
     * Salva um novo feedback no banco de dados.
     * Usa NOW() para preencher created_at e updated_at.
     */
    public void salvar(Feedback feedback) {
        String sql = "INSERT INTO feedback (versao_id, professor_id, status, comentario, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, feedback.getVersaoId());
            stmt.setInt(2, feedback.getProfessorId());
            stmt.setString(3, feedback.getStatus());
            stmt.setString(4, feedback.getComentario());

            stmt.executeUpdate();
            System.out.println("Feedback salvo com sucesso!");

        } catch (SQLException e) {
            // É melhor lançar a exceção para o controller saber que falhou
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar feedback: " + e.getMessage(), e);
        }
    }
}