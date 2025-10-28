package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Professor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessorDAO {

    /**
     * Cria a tabela professor caso não exista
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS professor (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                usuario_email VARCHAR(190) NOT NULL,
                cargo VARCHAR(150),
                tipo_tg ENUM('TG1','TG2','AMBOS','NENHUM') NOT NULL DEFAULT 'NENHUM',
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT uk_professor_usuario UNIQUE (usuario_email),
                CONSTRAINT fk_professor_usuario FOREIGN KEY (usuario_email)
                    REFERENCES usuario(email) ON DELETE RESTRICT ON UPDATE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela professor: " + e.getMessage(), e);
        }
    }

    /**
     * Insere um novo professor
     */
    public boolean inserirProfessor(Professor professor) {
        String sql = "INSERT INTO professor (usuario_email, cargo, tipo_tg) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, professor.getUsuarioEmail());
            stmt.setString(2, professor.getCargo());
            stmt.setString(3, professor.getTipoTG());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir professor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atualiza um professor existente pelo email do usuário
     */
    public boolean atualizarProfessor(Professor professor) {
        String sql = "UPDATE professor SET cargo = ?, tipo_tg = ? WHERE usuario_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, professor.getCargo());
            stmt.setString(2, professor.getTipoTG());
            stmt.setString(3, professor.getUsuarioEmail());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar professor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca um professor pelo email do usuário
     */
    public Professor findByUsuarioEmail(String email) {
        String sql = "SELECT * FROM professor WHERE usuario_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Professor professor = new Professor();
                    professor.setId(rs.getLong("id"));
                    professor.setUsuarioEmail(rs.getString("usuario_email"));
                    professor.setCargo(rs.getString("cargo"));
                    professor.setTipoTG(rs.getString("tipo_tg"));
                    professor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    professor.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return professor;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar professor: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Lista todos os professores
     */
    public List<Professor> listarProfessores() {
        List<Professor> professores = new ArrayList<>();
        String sql = "SELECT * FROM professor";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Professor professor = new Professor();
                professor.setId(rs.getLong("id"));
                professor.setUsuarioEmail(rs.getString("usuario_email"));
                professor.setCargo(rs.getString("cargo"));
                professor.setTipoTG(rs.getString("tipo_tg"));
                professor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                professor.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                professores.add(professor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return professores;
    }
}
