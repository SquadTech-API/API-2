package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Solicitacao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SolicitacaoDAO {

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS solicitacao_orientacao (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                aluno_email VARCHAR(190) NOT NULL,
                professor_id BIGINT NOT NULL,
                mensagem TEXT NOT NULL,
                status ENUM('AGUARDANDO','ACEITO','RECUSADO') NOT NULL DEFAULT 'AGUARDANDO',
                data_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                data_resposta DATETIME NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT fk_solicitacao_usuario FOREIGN KEY (aluno_email)
                    REFERENCES usuario(email) ON DELETE RESTRICT ON UPDATE CASCADE,
                CONSTRAINT fk_solicitacao_professor FOREIGN KEY (professor_id)
                    REFERENCES professor(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                INDEX idx_solicitacao_professor (professor_id),
                INDEX idx_solicitacao_aluno (aluno_email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;
        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela solicitacao_orientacao: " + e.getMessage(), e);
        }
    }

    public Solicitacao insert(Solicitacao s) {
        String sql = """
            INSERT INTO solicitacao_orientacao (aluno_email, professor_id, mensagem, status, data_envio)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getAlunoEmail());
            ps.setLong(2, s.getProfessorId());
            ps.setString(3, s.getMensagem());
            ps.setString(4, s.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(s.getDataEnvio() != null ? s.getDataEnvio() : LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setId(rs.getLong(1));
            }
            return s;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir solicitacao: " + e.getMessage(), e);
        }
    }

    public void atualizarStatus(Long id, String novoStatus) {
        String sql = "UPDATE solicitacao_orientacao SET status = ?, data_resposta = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novoStatus);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status da solicitacao: " + e.getMessage(), e);
        }
    }

    public Solicitacao findPendingByAlunoEmail(String alunoEmail) {
        String sql = "SELECT * FROM solicitacao_orientacao WHERE aluno_email = ? AND status = 'AGUARDANDO' ORDER BY data_envio DESC LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, alunoEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar solicitacao pendente por aluno: " + e.getMessage(), e);
        }
        return null;
    }

    public Solicitacao findLatestByAlunoEmail(String alunoEmail) {
        String sql = "SELECT * FROM solicitacao_orientacao WHERE aluno_email = ? ORDER BY data_envio DESC LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, alunoEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ultima solicitacao do aluno: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Solicitacao> listByProfessor(Long professorId) {
        String sql = "SELECT * FROM solicitacao_orientacao WHERE professor_id = ? ORDER BY data_envio DESC";
        List<Solicitacao> list = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, professorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar solicitacoes por professor: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Solicitacao> listByProfessorAndStatus(Long professorId, String status) {
        String sql = "SELECT * FROM solicitacao_orientacao WHERE professor_id = ? AND status = ? ORDER BY data_envio DESC";
        List<Solicitacao> list = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, professorId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar solicitacoes por professor e status: " + e.getMessage(), e);
        }
        return list;
    }

    public void deleteById(long id) {
        final String sql = "DELETE FROM solicitacao_orientacao WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao excluir solicitação: " + e.getMessage(), e);
        }
    }

    private Solicitacao map(ResultSet rs) throws SQLException {
        Solicitacao s = new Solicitacao();
        s.setId(rs.getLong("id"));
        s.setAlunoEmail(rs.getString("aluno_email"));
        s.setProfessorId(rs.getLong("professor_id"));
        s.setMensagem(rs.getString("mensagem"));
        s.setStatus(rs.getString("status"));
        Timestamp env = rs.getTimestamp("data_envio");
        if (env != null) s.setDataEnvio(env.toLocalDateTime());
        Timestamp resp = rs.getTimestamp("data_resposta");
        if (resp != null) s.setDataResposta(resp.toLocalDateTime());
        return s;
    }
}
