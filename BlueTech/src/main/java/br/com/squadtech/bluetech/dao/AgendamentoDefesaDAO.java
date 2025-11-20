package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.AgendamentoDefesa;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** DAO responsável pelo fluxo de agendamento de defesa (negociação aluno-professor). */
public class AgendamentoDefesaDAO {

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS agendamento_defesa (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                aluno_email VARCHAR(190) NOT NULL,
                professor_id BIGINT NOT NULL,
                mensagem_aluno TEXT NULL,
                mensagem_professor TEXT NULL,
                proposta_data_hora DATETIME NULL,
                sala VARCHAR(60) NULL,
                status ENUM('AGUARDANDO_ORIENTADOR','AGUARDANDO_ALUNO','REAGENDAMENTO','AGENDADO') NOT NULL DEFAULT 'AGUARDANDO_ORIENTADOR',
                data_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                data_atualizacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                data_confirmacao DATETIME NULL,
                CONSTRAINT fk_agendamento_aluno FOREIGN KEY (aluno_email) REFERENCES usuario(email) ON DELETE RESTRICT ON UPDATE CASCADE,
                CONSTRAINT fk_agendamento_professor FOREIGN KEY (professor_id) REFERENCES professor(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                INDEX idx_agendamento_professor (professor_id),
                INDEX idx_agendamento_aluno (aluno_email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;
        try (Connection c = ConnectionFactory.getConnection(); Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela agendamento_defesa: " + e.getMessage(), e);
        }
    }

    public AgendamentoDefesa inserirSolicitacao(AgendamentoDefesa a) {
        String sql = """
            INSERT INTO agendamento_defesa (aluno_email, professor_id, mensagem_aluno, status)
            VALUES (?, ?, ?, ?)
        """;
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getAlunoEmail());
            ps.setLong(2, a.getProfessorId());
            ps.setString(3, a.getMensagemAluno());
            ps.setString(4, a.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.setId(rs.getLong(1));
            }
            return a;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir solicitação de defesa: " + e.getMessage(), e);
        }
    }

    public AgendamentoDefesa findLatestByAluno(String emailAluno) {
        String sql = "SELECT * FROM agendamento_defesa WHERE aluno_email = ? ORDER BY data_criacao DESC LIMIT 1";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, emailAluno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar último agendamento do aluno: " + e.getMessage(), e);
        }
        return null;
    }

    public List<AgendamentoDefesa> listByProfessor(Long professorId) {
        String sql = "SELECT * FROM agendamento_defesa WHERE professor_id = ? ORDER BY data_criacao DESC";
        List<AgendamentoDefesa> list = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, professorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar agendamentos por professor: " + e.getMessage(), e);
        }
        return list;
    }

    public void proporAgendamento(Long id, String mensagemProfessor, LocalDate data, LocalTime hora, String sala, boolean reagendamento) {
        String novoStatus = reagendamento ? "REAGENDAMENTO" : "AGUARDANDO_ALUNO";
        String sql = """
            UPDATE agendamento_defesa
            SET mensagem_professor = ?, proposta_data_hora = ?, sala = ?, status = ?, data_atualizacao = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        LocalDateTime dt = null;
        if (data != null && hora != null) dt = data.atTime(hora);
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, mensagemProfessor);
            if (dt != null) ps.setTimestamp(2, Timestamp.valueOf(dt)); else ps.setNull(2, Types.TIMESTAMP);
            ps.setString(3, sala);
            ps.setString(4, novoStatus);
            ps.setLong(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao propor agendamento: " + e.getMessage(), e);
        }
    }

    public void alunoConfirma(Long id) {
        String sql = """
            UPDATE agendamento_defesa
            SET status = 'AGENDADO', data_confirmacao = CURRENT_TIMESTAMP, data_atualizacao = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao confirmar agendamento: " + e.getMessage(), e);
        }
    }

    public void alunoRecusa(Long id, String mensagemAluno) {
        String sql = """
            UPDATE agendamento_defesa
            SET mensagem_aluno = ?, status = 'AGUARDANDO_ORIENTADOR', data_atualizacao = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, mensagemAluno);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao recusar proposta: " + e.getMessage(), e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM agendamento_defesa WHERE id = ?";
        try (Connection c = ConnectionFactory.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir agendamento defesa: " + e.getMessage(), e);
        }
    }

    private AgendamentoDefesa map(ResultSet rs) throws SQLException {
        AgendamentoDefesa a = new AgendamentoDefesa();
        a.setId(rs.getLong("id"));
        a.setAlunoEmail(rs.getString("aluno_email"));
        a.setProfessorId(rs.getLong("professor_id"));
        a.setMensagemAluno(rs.getString("mensagem_aluno"));
        a.setMensagemProfessor(rs.getString("mensagem_professor"));
        Timestamp ts = rs.getTimestamp("proposta_data_hora");
        if (ts != null) a.setPropostaDataHora(ts.toLocalDateTime());
        a.setSala(rs.getString("sala"));
        a.setStatus(rs.getString("status"));
        Timestamp c = rs.getTimestamp("data_criacao");
        if (c != null) a.setDataCriacao(c.toLocalDateTime());
        Timestamp u = rs.getTimestamp("data_atualizacao");
        if (u != null) a.setDataAtualizacao(u.toLocalDateTime());
        Timestamp conf = rs.getTimestamp("data_confirmacao");
        if (conf != null) a.setDataConfirmacao(conf.toLocalDateTime());
        return a;
    }
}
