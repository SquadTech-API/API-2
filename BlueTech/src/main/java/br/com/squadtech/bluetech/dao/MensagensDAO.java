package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Mensagens;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável por persistir as mensagens trocadas entre aluno e orientador.
 */
public class MensagensDAO {

    private static final String BASE_SELECT = "SELECT data_hora, conteudo, professor_id, aluno_id, orientacao_id, secao_id, enviado_por_professor FROM mensagens";

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS mensagens (
                data_hora DATETIME(3) NOT NULL,
                conteudo TEXT NOT NULL,
                professor_id BIGINT NOT NULL,
                aluno_id INT NOT NULL,
                orientacao_id BIGINT NOT NULL,
                secao_id INT NOT NULL,
                enviado_por_professor BOOLEAN NOT NULL,
                PRIMARY KEY (data_hora),
                INDEX idx_msg_prof_aluno (professor_id, aluno_id),
                INDEX idx_msg_secao (secao_id),
                CONSTRAINT fk_msg_professor FOREIGN KEY (professor_id) REFERENCES professor(id) ON DELETE CASCADE,
                CONSTRAINT fk_msg_aluno FOREIGN KEY (aluno_id) REFERENCES Perfil_Aluno(id_perfil_aluno) ON DELETE CASCADE,
                CONSTRAINT fk_msg_orienta FOREIGN KEY (orientacao_id) REFERENCES orienta(id) ON DELETE CASCADE,
                CONSTRAINT fk_msg_secao FOREIGN KEY (secao_id) REFERENCES TG_Secao(Id_Secao) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela mensagens: " + e.getMessage(), e);
        }
    }

    public void ensureSchemaUpToDate() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (!columnExists(conn, "mensagens", "secao_id")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE mensagens ADD COLUMN secao_id INT NOT NULL AFTER orientacao_id");
                    st.executeUpdate("ALTER TABLE mensagens ADD CONSTRAINT fk_msg_secao FOREIGN KEY (secao_id) REFERENCES TG_Secao(Id_Secao) ON DELETE CASCADE");
                }
            }
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE INDEX idx_msg_secao ON mensagens(secao_id)");
            } catch (SQLException ignored) {}
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao migrar tabela mensagens: " + e.getMessage(), e);
        }
    }

    private boolean columnExists(Connection conn, String table, String column) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void salvar(Mensagens mensagem) {
        String sql = """
            INSERT INTO mensagens (data_hora, conteudo, professor_id, aluno_id, orientacao_id, secao_id, enviado_por_professor)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(mensagem.getDataHora()));
            stmt.setString(2, mensagem.getConteudo());
            stmt.setLong(3, mensagem.getProfessorId());
            stmt.setLong(4, mensagem.getAlunoId());
            stmt.setLong(5, mensagem.getOrientacaoId());
            stmt.setInt(6, mensagem.getSecaoId());
            stmt.setBoolean(7, Boolean.TRUE.equals(mensagem.getEnviadoPorProfessor()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar mensagem: " + e.getMessage(), e);
        }
    }

    public List<Mensagens> listarChat(Long professorId, Long alunoId, Integer secaoId, int limit) {
        String sql = BASE_SELECT + " WHERE professor_id = ? AND aluno_id = ? AND secao_id = ? ORDER BY data_hora ASC LIMIT ?";
        List<Mensagens> mensagens = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, professorId);
            stmt.setLong(2, alunoId);
            stmt.setInt(3, secaoId);
            stmt.setInt(4, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mensagens.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mensagens: " + e.getMessage(), e);
        }

        return mensagens;
    }

    public List<Mensagens> listarChatApos(Long professorId, Long alunoId, Integer secaoId, LocalDateTime dataReferencia, int limit) {
        String sql = BASE_SELECT + " WHERE professor_id = ? AND aluno_id = ? AND secao_id = ? AND data_hora > ? ORDER BY data_hora ASC LIMIT ?";
        List<Mensagens> mensagens = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, professorId);
            stmt.setLong(2, alunoId);
            stmt.setInt(3, secaoId);
            stmt.setTimestamp(4, Timestamp.valueOf(dataReferencia));
            stmt.setInt(5, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mensagens.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mensagens por data: " + e.getMessage(), e);
        }

        return mensagens;
    }

    public LocalDateTime buscarUltimaData(Long professorId, Long alunoId, Integer secaoId) {
        String sql = "SELECT data_hora FROM mensagens WHERE professor_id = ? AND aluno_id = ? AND secao_id = ? ORDER BY data_hora DESC LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, professorId);
            stmt.setLong(2, alunoId);
            stmt.setInt(3, secaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("data_hora");
                    return ts != null ? ts.toLocalDateTime() : null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar última data de mensagem: " + e.getMessage(), e);
        }
        return null;
    }

    private Mensagens mapear(ResultSet rs) throws SQLException {
        Mensagens msg = new Mensagens();
        Timestamp ts = rs.getTimestamp("data_hora");
        msg.setDataHora(ts != null ? ts.toLocalDateTime() : null);
        msg.setConteudo(rs.getString("conteudo"));
        msg.setProfessorId(rs.getLong("professor_id"));
        msg.setAlunoId(rs.getInt("aluno_id"));
        msg.setOrientacaoId(rs.getLong("orientacao_id"));
        msg.setSecaoId(rs.getInt("secao_id"));
        msg.setEnviadoPorProfessor(rs.getBoolean("enviado_por_professor"));
        return msg;
    }

    public boolean existeRegistroComData(LocalDateTime dataHora) {
        String sql = "SELECT 1 FROM mensagens WHERE data_hora = ? LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(dataHora));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existência de mensagem: " + e.getMessage(), e);
        }
    }
}
