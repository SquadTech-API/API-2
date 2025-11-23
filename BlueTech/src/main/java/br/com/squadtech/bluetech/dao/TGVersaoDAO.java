package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.TGVersao;
import br.com.squadtech.bluetech.util.MarkdownParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TGVersaoDAO {

    private static final Logger log = LoggerFactory.getLogger(TGVersaoDAO.class);

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS TG_Versao (" +
                "Id_Versao INT AUTO_INCREMENT PRIMARY KEY," +
                "Id_Secao INT NULL," +
                "Versao_Numero INT NULL," +
                "Markdown_Content LONGTEXT NULL," +
                "Semestre_Curso VARCHAR(50)," +
                "Ano INT," +
                "Semestre_Ano VARCHAR(50)," +
                "Empresa_Parceira VARCHAR(255)," +
                "Problema TEXT," +
                "Solucao TEXT," +
                "Link_Repositorio VARCHAR(255)," +
                "Link_Linkedin VARCHAR(255)," +
                "Tecnologias TEXT," +
                "Contribuicoes TEXT," +
                "Hard_Skills TEXT," +
                "Soft_Skills TEXT," +
                "Data_Criacao DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela TG_Versao: " + e.getMessage(), e);
        }
    }

    public void ensureSchemaUpToDate() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (!columnExists(conn, "TG_Versao", "Id_Secao")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE TG_Versao ADD COLUMN Id_Secao INT NULL");
                }
            }
            if (!columnExists(conn, "TG_Versao", "Versao_Numero")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE TG_Versao ADD COLUMN Versao_Numero INT NULL");
                }
                // Backfill Versao_Numero por seção, ordenado por Data_Criacao/Id_Versao
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT Id_Secao FROM TG_Versao WHERE Id_Secao IS NOT NULL GROUP BY Id_Secao");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idSecao = rs.getInt(1);
                        backfillVersaoNumeroForSecao(conn, idSecao);
                    }
                }
            }
            if (!columnExists(conn, "TG_Versao", "Markdown_Content")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE TG_Versao ADD COLUMN Markdown_Content LONGTEXT NULL");
                }
            }
            if (!columnExists(conn, "TG_Versao", "Data_Criacao")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE TG_Versao ADD COLUMN Data_Criacao DATETIME NULL");
                    st.executeUpdate("UPDATE TG_Versao SET Data_Criacao = NOW() WHERE Data_Criacao IS NULL");
                    try { st.executeUpdate("ALTER TABLE TG_Versao MODIFY COLUMN Data_Criacao DATETIME DEFAULT CURRENT_TIMESTAMP"); } catch (SQLException ignored) {}
                }
            }
            // Backfill Semestre_Ano partindo de Semestre_Curso quando estiver nulo, para evitar mostrar 'null' nos cards
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("UPDATE TG_Versao SET Semestre_Ano = Semestre_Curso WHERE Semestre_Ano IS NULL AND Semestre_Curso IS NOT NULL");
            } catch (SQLException ignored) {}

            // Backfill de campos estruturados a partir do Markdown, para versões legadas que só têm Markdown_Content
            backfillStructuredFieldsFromMarkdown(conn);

            try (Statement st = conn.createStatement()) {
                try { st.executeUpdate("CREATE INDEX idx_tgversao_id_secao ON TG_Versao(Id_Secao)"); } catch (SQLException ignored) {}
            }
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("UPDATE TG_Versao v JOIN TG_Secao s ON s.Id_Versao = v.Id_Versao SET v.Id_Secao = s.Id_Secao WHERE v.Id_Secao IS NULL");
            } catch (SQLException ignored) {}
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao migrar schema de TG_Versao: " + e.getMessage(), e);
        }
    }

    private void backfillStructuredFieldsFromMarkdown(Connection conn) {
        String select = "SELECT Id_Versao, Markdown_Content FROM TG_Versao WHERE Markdown_Content IS NOT NULL AND (Semestre_Curso IS NULL OR Ano IS NULL OR Semestre_Ano IS NULL)";
        try (PreparedStatement ps = conn.prepareStatement(select);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("Id_Versao");
                String md = rs.getString("Markdown_Content");
                if (md == null || md.isBlank()) continue;
                TGVersao temp = new TGVersao();
                MarkdownParserUtil.parseMarkdownIntoFields(temp, md);
                // Se conseguiu derivar algo, atualiza
                String update = "UPDATE TG_Versao SET Semestre_Curso = COALESCE(?, Semestre_Curso), Ano = COALESCE(?, Ano), Semestre_Ano = COALESCE(?, Semestre_Ano) WHERE Id_Versao = ?";
                try (PreparedStatement up = conn.prepareStatement(update)) {
                    up.setString(1, temp.getSemestre());
                    if (temp.getAno() != null) up.setInt(2, temp.getAno()); else up.setNull(2, Types.INTEGER);
                    up.setString(3, temp.getSemestreAno());
                    up.setInt(4, id);
                    up.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // não interrompe migração por falhas pontuais
            log.warn("Backfill de campos estruturados a partir do Markdown falhou: {}", e.getMessage());
        }
    }

    private void backfillVersaoNumeroForSecao(Connection conn, int idSecao) throws SQLException {
        // Ordena por Data_Criacao (se existir), fallback Id_Versao, e enumera 1..n
        String q = "SELECT Id_Versao FROM TG_Versao WHERE Id_Secao = ? ORDER BY COALESCE(Data_Criacao, '1970-01-01'), Id_Versao";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, idSecao);
            try (ResultSet rs = ps.executeQuery()) {
                int n = 1;
                while (rs.next()) {
                    int idVersao = rs.getInt(1);
                    try (PreparedStatement up = conn.prepareStatement("UPDATE TG_Versao SET Versao_Numero = ? WHERE Id_Versao = ?")) {
                        up.setInt(1, n++);
                        up.setInt(2, idVersao);
                        up.executeUpdate();
                    }
                }
            }
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

    private int nextVersaoNumero(Connection conn, Integer idSecao) throws SQLException {
        if (idSecao == null) return 1;
        String sql = "SELECT COALESCE(MAX(Versao_Numero), 0) + 1 FROM TG_Versao WHERE Id_Secao = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSecao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 1;
    }

    public Integer insertReturningId(TGVersao secao) {
        String sql = "INSERT INTO TG_Versao (Id_Secao, Versao_Numero, Markdown_Content, Semestre_Curso, Ano, Semestre_Ano, Empresa_Parceira, Problema, Solucao, Link_Repositorio, Link_Linkedin, Tecnologias, Contribuicoes, Hard_Skills, Soft_Skills) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (secao.getIdSecao() != null) stmt.setInt(1, secao.getIdSecao()); else stmt.setNull(1, java.sql.Types.INTEGER);
            // Calcula Versao_Numero: se já veio definido, usa; senão calcula por seção
            Integer vnum = secao.getVersaoNumero();
            if (vnum == null) vnum = nextVersaoNumero(conn, secao.getIdSecao());
            stmt.setInt(2, vnum);

            // Markdown completo (pode ser nulo)
            stmt.setString(3, secao.getMarkdownContent());

            stmt.setString(4, secao.getSemestre());
            if (secao.getAno() != null) stmt.setInt(5, secao.getAno()); else stmt.setNull(5, java.sql.Types.INTEGER);
            // Garante Semestre_Ano não-nulo quando possível
            String semestreAno = secao.getSemestreAno();
            if (semestreAno == null || semestreAno.isBlank()) {
                semestreAno = secao.getSemestre();
            }
            stmt.setString(6, semestreAno);
            stmt.setString(7, secao.getEmpresa());
            stmt.setString(8, secao.getProblema());
            stmt.setString(9, secao.getSolucao());
            stmt.setString(10, secao.getRepositorio());
            stmt.setString(11, secao.getLinkedin());
            stmt.setString(12, secao.getTecnologias());
            stmt.setString(13, secao.getContribuicoes());
            stmt.setString(14, secao.getHardSkills());
            stmt.setString(15, secao.getSoftSkills());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } catch (SQLException e) {
            log.error("Erro ao inserir TG_Versao", e);
            return null;
        }
    }

    public boolean insert(TGVersao secao) {
        return insertReturningId(secao) != null;
    }

    public Integer buscarIdSecaoPorIdVersao(int idVersao) {
        String sql = "SELECT `Id_Secao` FROM `TG_Versao` WHERE `Id_Versao` = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVersao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idSecao = rs.getInt("Id_Secao");
                    if (!rs.wasNull()) return idSecao;
                }
            }
        } catch (SQLException e) {
            log.error("Erro ao buscar Id_Secao por Id_Versao", e);
        }
        return null;
    }

    public TGVersao findById(int idVersao) {
        String sql = "SELECT Id_Versao, Id_Secao, Versao_Numero, Markdown_Content, Semestre_Curso, Ano, Semestre_Ano, Empresa_Parceira, Problema, Solucao, Link_Repositorio, Link_Linkedin, Tecnologias, Contribuicoes, Hard_Skills, Soft_Skills, Data_Criacao FROM TG_Versao WHERE Id_Versao = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVersao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TGVersao v = new TGVersao();
                    v.setIdSecaoApi(rs.getInt("Id_Versao"));
                    int idSecao = rs.getInt("Id_Secao");
                    if (!rs.wasNull()) v.setIdSecao(idSecao);
                    int vn = rs.getInt("Versao_Numero");
                    if (!rs.wasNull()) v.setVersaoNumero(vn);
                    v.setMarkdownContent(rs.getString("Markdown_Content"));
                    v.setSemestre(rs.getString("Semestre_Curso"));
                    int ano = rs.getInt("Ano");
                    if (!rs.wasNull()) v.setAno(ano);
                    v.setSemestreAno(rs.getString("Semestre_Ano"));
                    v.setEmpresa(rs.getString("Empresa_Parceira"));
                    v.setProblema(rs.getString("Problema"));
                    v.setSolucao(rs.getString("Solucao"));
                    v.setRepositorio(rs.getString("Link_Repositorio"));
                    v.setLinkedin(rs.getString("Link_Linkedin"));
                    v.setTecnologias(rs.getString("Tecnologias"));
                    v.setContribuicoes(rs.getString("Contribuicoes"));
                    v.setHardSkills(rs.getString("Hard_Skills"));
                    v.setSoftSkills(rs.getString("Soft_Skills"));
                    Timestamp t = rs.getTimestamp("Data_Criacao");
                    if (t != null) v.setCreatedAt(t.toLocalDateTime());
                    return v;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar TG_Versao por id: " + e.getMessage(), e);
        }
        return null;
    }

    public List<TGVersao> listBySecaoId(int idSecao) {
        String sql = "SELECT Id_Versao, Id_Secao, Versao_Numero, Markdown_Content, Semestre_Curso, Ano, Semestre_Ano, Empresa_Parceira, Problema, Solucao, Link_Repositorio, Link_Linkedin, Tecnologias, Contribuicoes, Hard_Skills, Soft_Skills, Data_Criacao FROM TG_Versao WHERE Id_Secao = ? ORDER BY Data_Criacao DESC, Id_Versao DESC";
        List<TGVersao> list = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSecao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TGVersao v = new TGVersao();
                    v.setIdSecaoApi(rs.getInt("Id_Versao"));
                    int idS = rs.getInt("Id_Secao");
                    if (!rs.wasNull()) v.setIdSecao(idS);
                    int vn = rs.getInt("Versao_Numero");
                    if (!rs.wasNull()) v.setVersaoNumero(vn);
                    v.setMarkdownContent(rs.getString("Markdown_Content"));
                    v.setSemestre(rs.getString("Semestre_Curso"));
                    int ano = rs.getInt("Ano");
                    if (!rs.wasNull()) v.setAno(ano);
                    v.setSemestreAno(rs.getString("Semestre_Ano"));
                    v.setEmpresa(rs.getString("Empresa_Parceira"));
                    v.setProblema(rs.getString("Problema"));
                    v.setSolucao(rs.getString("Solucao"));
                    v.setRepositorio(rs.getString("Link_Repositorio"));
                    v.setLinkedin(rs.getString("Link_Linkedin"));
                    v.setTecnologias(rs.getString("Tecnologias"));
                    v.setContribuicoes(rs.getString("Contribuicoes"));
                    v.setHardSkills(rs.getString("Hard_Skills"));
                    v.setSoftSkills(rs.getString("Soft_Skills"));
                    Timestamp t = rs.getTimestamp("Data_Criacao");
                    if (t != null) v.setCreatedAt(t.toLocalDateTime());
                    list.add(v);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar TG_Versao por Id_Secao: " + e.getMessage(), e);
        }
        return list;
    }

    public void updateSecaoId(int idVersao, int idSecao) {
        String sql = "UPDATE TG_Versao SET Id_Secao = ? WHERE Id_Versao = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idSecao > 0) ps.setInt(1, idSecao); else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setInt(2, idVersao);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar Id_Secao em TG_Versao: " + e.getMessage(), e);
        }
        // Após vincular uma versão preexistente a uma seção, atribui Versao_Numero se estiver nulo
        try (Connection conn = ConnectionFactory.getConnection()) {
            try (PreparedStatement ck = conn.prepareStatement("SELECT Versao_Numero FROM TG_Versao WHERE Id_Versao = ?")) {
                ck.setInt(1, idVersao);
                try (ResultSet rs = ck.executeQuery()) {
                    if (rs.next()) {
                        int vn = rs.getInt(1);
                        if (rs.wasNull()) {
                            int next = nextVersaoNumero(conn, idSecao);
                            try (PreparedStatement up = conn.prepareStatement("UPDATE TG_Versao SET Versao_Numero = ? WHERE Id_Versao = ?")) {
                                up.setInt(1, next);
                                up.setInt(2, idVersao);
                                up.executeUpdate();
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao atribuir Versao_Numero após atualizar Id_Secao: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atribuir Versao_Numero após atualizar Id_Secao: " + e.getMessage(), e);
        }
    }

    public void updateMarkdownContent(int idVersao, String markdownContent) {
        String sql = "UPDATE TG_Versao SET Markdown_Content = ? WHERE Id_Versao = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, markdownContent);
            ps.setInt(2, idVersao);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar Markdown da TG_Versao: " + e.getMessage(), e);
        }
    }
}
