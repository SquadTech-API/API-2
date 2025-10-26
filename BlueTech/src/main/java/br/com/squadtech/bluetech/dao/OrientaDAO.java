package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Orienta;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrientaDAO {

    /**
     * Cria a tabela orienta caso não exista.
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS orienta (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                professor_id BIGINT NOT NULL,
                aluno_id BIGINT NOT NULL,
                ativo BOOLEAN NOT NULL DEFAULT TRUE,
                data_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                data_fim DATETIME NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT uk_orientacao_prof_aluno UNIQUE (professor_id, aluno_id),
                CONSTRAINT fk_orienta_professor FOREIGN KEY (professor_id)
                    REFERENCES professor(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                CONSTRAINT fk_orienta_aluno FOREIGN KEY (aluno_id)
                    REFERENCES perfil_aluno(id) ON DELETE RESTRICT ON UPDATE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela orienta: " + e.getMessage(), e);
        }
    }

    /**
     * Insere uma nova orientação.
     */
    public void insert(Orienta orienta) {
        String sql = "INSERT INTO orienta (professor_id, aluno_id, ativo, data_inicio) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, orienta.getProfessorId());
            stmt.setLong(2, orienta.getAlunoId());
            stmt.setBoolean(3, orienta.isAtivo());
            stmt.setTimestamp(4, Timestamp.valueOf(orienta.getDataInicio()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    orienta.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir orientação: " + e.getMessage(), e);
        }
    }

    /**
     * Busca orientações por professor.
     */
    public List<Orienta> findByProfessorId(Long professorId) {
        List<Orienta> lista = new ArrayList<>();
        String sql = "SELECT * FROM orienta WHERE professor_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, professorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar orientações por professor: " + e.getMessage(), e);
        }

        return lista;
    }

    /**
     * Busca orientações por aluno.
     */
    public List<Orienta> findByAlunoId(Long alunoId) {
        List<Orienta> lista = new ArrayList<>();
        String sql = "SELECT * FROM orienta WHERE aluno_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar orientações por aluno: " + e.getMessage(), e);
        }

        return lista;
    }

    /**
     * Retorna todas as orientações.
     */
    public List<Orienta> findAll() {
        List<Orienta> lista = new ArrayList<>();
        String sql = "SELECT * FROM orienta";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todas orientações: " + e.getMessage(), e);
        }

        return lista;
    }

    /**
     * Atualiza uma orientação (ex: data_fim ou status).
     */
    public void update(Orienta orienta) {
        String sql = "UPDATE orienta SET ativo = ?, data_fim = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, orienta.isAtivo());
            if (orienta.getDataFim() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(orienta.getDataFim()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            stmt.setLong(3, orienta.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar orientação: " + e.getMessage(), e);
        }
    }

    /**
     * Desativa uma orientação (delete lógico).
     */
    public void delete(Long id) {
        String sql = "UPDATE orienta SET ativo = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar orientação: " + e.getMessage(), e);
        }
    }

    /**
     * Mapeia o ResultSet para objeto Orienta.
     */
    private Orienta mapResultSet(ResultSet rs) throws SQLException {
        Orienta o = new Orienta();
        o.setId(rs.getLong("id"));
        o.setProfessorId(rs.getLong("professor_id"));
        o.setAlunoId(rs.getLong("aluno_id"));
        o.setAtivo(rs.getBoolean("ativo"));
        o.setDataInicio(rs.getTimestamp("data_inicio").toLocalDateTime());

        Timestamp dataFimTS = rs.getTimestamp("data_fim");
        if (dataFimTS != null) {
            o.setDataFim(dataFimTS.toLocalDateTime());
        }

        o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        o.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return o;
    }
}
