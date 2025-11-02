package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.TGSecao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TGSecaoDAO {

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS TG_Secao (" +
                "Id_Secao INT AUTO_INCREMENT PRIMARY KEY," +
                "API_Numero INT NOT NULL," +
                "Data_Envio DATETIME NOT NULL," +
                "Data_Aprovacao DATETIME NULL," +
                "Status VARCHAR(50) NOT NULL," +
                "Id_Versao INT NOT NULL," +
                "email_usuario VARCHAR(255) NOT NULL," +
                "CONSTRAINT fk_tgsecao_versao FOREIGN KEY (Id_Versao) REFERENCES TG_Versao(Id_Versao)," +
                "CONSTRAINT fk_tgsecao_usuario FOREIGN KEY (email_usuario) REFERENCES usuario(email) ON DELETE RESTRICT ON UPDATE CASCADE" +
                ")";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            // Índice de unicidade por usuário+API
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS uk_tgsecao_user_api ON TG_Secao(email_usuario, API_Numero)");
            } catch (SQLException ignore) {
                try (Statement st2 = conn.createStatement()) {
                    st2.executeUpdate("ALTER TABLE TG_Secao ADD UNIQUE INDEX uk_tgsecao_user_api (email_usuario, API_Numero)");
                } catch (SQLException ignored) {}
            }
            // Remover antigo índice único por API se existir, para permitir APIs por usuário
            try (Statement st3 = conn.createStatement()) {
                st3.executeUpdate("DROP INDEX uk_tgsecao_api ON TG_Secao");
            } catch (SQLException ignored) {}
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela TG_Secao: " + e.getMessage(), e);
        }
    }

    // Verifica se já existe seção para a API do usuário
    public boolean existsByApiNumero(int apiNumero, String emailUsuario) {
        String sql = "SELECT 1 FROM TG_Secao WHERE API_Numero = ? AND email_usuario = ? LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apiNumero);
            ps.setString(2, emailUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existência por API_Numero/email: " + e.getMessage(), e);
        }
    }

    // Conta quantas seções o usuário possui
    public int countSecoes(String emailUsuario) {
        String sql = "SELECT COUNT(*) FROM TG_Secao WHERE email_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar TG_Secao do usuário: " + e.getMessage(), e);
        }
    }

    // Insere uma nova seção vinculada ao usuário
    public int insert(TGSecao secao) {
        String insert = "INSERT INTO TG_Secao (API_Numero, Data_Envio, Data_Aprovacao, Status, Id_Versao, email_usuario) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement psInsert = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            psInsert.setInt(1, secao.getApiNumero());
            psInsert.setTimestamp(2, Timestamp.valueOf(secao.getDataEnvio()));
            if (secao.getDataAprovacao() != null) psInsert.setTimestamp(3, Timestamp.valueOf(secao.getDataAprovacao()));
            else psInsert.setNull(3, Types.TIMESTAMP);
            psInsert.setString(4, secao.getStatus() == null ? "Em andamento" : secao.getStatus());
            psInsert.setInt(5, secao.getIdVersao());
            psInsert.setString(6, secao.getEmailUsuario());
            psInsert.executeUpdate();
            try (ResultSet rs = psInsert.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir TG_Secao: " + e.getMessage(), e);
        }
    }

    // Exclui seção garantindo que pertence ao usuário
    public void deleteByIdAndEmail(int idSecao, String emailUsuario) {
        String sql = "DELETE FROM TG_Secao WHERE Id_Secao = ? AND email_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSecao);
            ps.setString(2, emailUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir TG_Secao: " + e.getMessage(), e);
        }
    }

    // Lista cards apenas do usuário
    public List<CardDados> listarCards(String emailUsuario) {
        String sql = "SELECT s.Id_Secao, s.API_Numero, s.Data_Envio, s.Status, v.Semestre_Curso, v.Ano, COALESCE(NULLIF(v.Semestre_Ano, ''), v.Semestre_Curso) AS Semestre_Ano " +
                "FROM TG_Secao s " +
                "JOIN TG_Versao v ON v.Id_Versao = s.Id_Versao " +
                "WHERE s.email_usuario = ? " +
                "ORDER BY s.API_Numero";
        List<CardDados> list = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CardDados c = new CardDados();
                    c.idSecao = rs.getInt("Id_Secao");
                    c.apiNumero = rs.getInt("API_Numero");
                    c.dataEnvio = rs.getTimestamp("Data_Envio").toLocalDateTime();
                    c.status = rs.getString("Status");
                    c.semestreCurso = rs.getString("Semestre_Curso");
                    c.ano = rs.getInt("Ano");
                    c.semestreAno = rs.getString("Semestre_Ano");
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar cards de TG_Secao: " + e.getMessage(), e);
        }
        return list;
    }

    // DTO para dados dos cards
    public static class CardDados {
        public int idSecao;
        public int apiNumero;
        public LocalDateTime dataEnvio;
        public String status;
        public String semestreCurso;
        public int ano;
        public String semestreAno;
    }

    // Busca por id garantindo dono
    public TGSecao findByIdAndEmail(int idSecao, String emailUsuario) {
        String sql = "SELECT Id_Secao, API_Numero, Data_Envio, Data_Aprovacao, Status, Id_Versao, email_usuario FROM TG_Secao WHERE Id_Secao = ? AND email_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSecao);
            ps.setString(2, emailUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TGSecao s = new TGSecao();
                    s.setIdSecao(rs.getInt("Id_Secao"));
                    s.setApiNumero(rs.getInt("API_Numero"));
                    Timestamp t = rs.getTimestamp("Data_Envio");
                    if (t != null) s.setDataEnvio(t.toLocalDateTime());
                    Timestamp t2 = rs.getTimestamp("Data_Aprovacao");
                    if (t2 != null) s.setDataAprovacao(t2.toLocalDateTime());
                    s.setStatus(rs.getString("Status"));
                    s.setIdVersao(rs.getInt("Id_Versao"));
                    s.setEmailUsuario(rs.getString("email_usuario"));
                    return s;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar TG_Secao por id/email: " + e.getMessage(), e);
        }
        return null;
    }

    // Método legado mantido para compatibilidade interna, mas recomenda-se usar findByIdAndEmail
    public TGSecao findById(int idSecao) {
        String sql = "SELECT Id_Secao, API_Numero, Data_Envio, Data_Aprovacao, Status, Id_Versao, email_usuario FROM TG_Secao WHERE Id_Secao = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSecao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TGSecao s = new TGSecao();
                    s.setIdSecao(rs.getInt("Id_Secao"));
                    s.setApiNumero(rs.getInt("API_Numero"));
                    Timestamp t = rs.getTimestamp("Data_Envio");
                    if (t != null) s.setDataEnvio(t.toLocalDateTime());
                    Timestamp t2 = rs.getTimestamp("Data_Aprovacao");
                    if (t2 != null) s.setDataAprovacao(t2.toLocalDateTime());
                    s.setStatus(rs.getString("Status"));
                    s.setIdVersao(rs.getInt("Id_Versao"));
                    s.setEmailUsuario(rs.getString("email_usuario"));
                    return s;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar TG_Secao por id: " + e.getMessage(), e);
        }
        return null;
    }

    public void ensureSchemaUpToDate() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            // Adiciona coluna email_usuario se não existir (permite NULL temporariamente para migração)
            if (!columnExists(conn, "TG_Secao", "email_usuario")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE TG_Secao ADD COLUMN email_usuario VARCHAR(255) NULL");
                }
                // Tenta criar FK (ignora falha se existir)
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE TG_Secao ADD CONSTRAINT fk_tgsecao_usuario FOREIGN KEY (email_usuario) REFERENCES usuario(email) ON DELETE RESTRICT ON UPDATE CASCADE");
                } catch (SQLException ignored) {}
            }
            // Cria índice único por usuário+API, se possível
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE UNIQUE INDEX uk_tgsecao_user_api ON TG_Secao(email_usuario, API_Numero)");
            } catch (SQLException ignored) {}
            // Dropa índice antigo por API se existir
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DROP INDEX uk_tgsecao_api ON TG_Secao");
            } catch (SQLException ignored) {}
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao migrar schema de TG_Secao: " + e.getMessage(), e);
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Atualiza o Id_Versao da seção (promove nova versão)
    public void updateIdVersao(int idSecao, int idVersao) {
        String sql = "UPDATE TG_Secao SET Id_Versao = ? WHERE Id_Secao = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVersao);
            ps.setInt(2, idSecao);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar Id_Versao em TG_Secao: " + e.getMessage(), e);
        }
    }

}
