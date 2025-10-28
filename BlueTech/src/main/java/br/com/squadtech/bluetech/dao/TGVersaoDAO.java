package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.TGVersao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TGVersaoDAO {

    /**
     * Cria a tabela tg_versao caso não exista
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS tg_versao (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                secao_id BIGINT NOT NULL,
                numero_versao INT NOT NULL,
                semestre VARCHAR(50),
                ano INT,
                semestre_ano VARCHAR(50),
                empresa VARCHAR(255),
                problema TEXT,
                solucao TEXT,
                repositorio VARCHAR(255),
                linkedin VARCHAR(255),
                tecnologias TEXT,
                contribuicoes TEXT,
                hard_skills TEXT,
                soft_skills TEXT,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT fk_versao_secao FOREIGN KEY (secao_id)
                    REFERENCES tg_secao(id) ON DELETE CASCADE ON UPDATE CASCADE,
                UNIQUE (secao_id, numero_versao)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela tg_versao: " + e.getMessage(), e);
        }
    }

    /**
     * Insere uma nova versão e retorna o ID gerado
     */
    public Long insertReturningId(TGVersao versao, int numeroVersao) {
        String sql = """
            INSERT INTO tg_versao
            (secao_id, numero_versao, semestre, ano, semestre_ano, empresa, problema, solucao,
            repositorio, linkedin, tecnologias, contribuicoes, hard_skills, soft_skills)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, versao.getSecaoId());
            stmt.setInt(2, numeroVersao);
            stmt.setString(3, versao.getSemestre());
            if (versao.getAno() != null) stmt.setInt(4, versao.getAno()); else stmt.setNull(4, Types.INTEGER);
            stmt.setString(5, versao.getSemestreAno());
            stmt.setString(6, versao.getEmpresa());
            stmt.setString(7, versao.getProblema());
            stmt.setString(8, versao.getSolucao());
            stmt.setString(9, versao.getRepositorio());
            stmt.setString(10, versao.getLinkedin());
            stmt.setString(11, versao.getTecnologias());
            stmt.setString(12, versao.getContribuicoes());
            stmt.setString(13, versao.getHardSkills());
            stmt.setString(14, versao.getSoftSkills());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir versão: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Retorna o último número de versão de uma seção
     */
    public int getUltimoNumeroVersao(Long secaoId) {
        String sql = "SELECT MAX(numero_versao) AS ultima_versao FROM tg_versao WHERE secao_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, secaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ultima_versao");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar última versão: " + e.getMessage(), e);
        }
        return 0; // se não existir, retorna 0
    }

    /**
     * Lista todas as versões de uma seção
     */
    public List<TGVersao> findBySecaoId(Long secaoId) {
        String sql = "SELECT * FROM tg_versao WHERE secao_id = ? ORDER BY numero_versao";
        List<TGVersao> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, secaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToTGVersao(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar versões da seção: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Mapeia ResultSet para objeto TGVersao
     */
    private TGVersao mapResultSetToTGVersao(ResultSet rs) throws SQLException {
        TGVersao versao = new TGVersao();
        versao.setId(rs.getLong("id"));
        versao.setSecaoId(rs.getLong("secao_id"));
        versao.setSemestre(rs.getString("semestre"));
        int ano = rs.getInt("ano");
        versao.setAno(rs.wasNull() ? null : ano);
        versao.setSemestreAno(rs.getString("semestre_ano"));
        versao.setEmpresa(rs.getString("empresa"));
        versao.setProblema(rs.getString("problema"));
        versao.setSolucao(rs.getString("solucao"));
        versao.setRepositorio(rs.getString("repositorio"));
        versao.setLinkedin(rs.getString("linkedin"));
        versao.setTecnologias(rs.getString("tecnologias"));
        versao.setContribuicoes(rs.getString("contribuicoes"));
        versao.setHardSkills(rs.getString("hard_skills"));
        versao.setSoftSkills(rs.getString("soft_skills"));

        Timestamp tsCreated = rs.getTimestamp("created_at");
        if (tsCreated != null) versao.setCreatedAt(tsCreated.toLocalDateTime());
        Timestamp tsUpdated = rs.getTimestamp("updated_at");
        if (tsUpdated != null) versao.setUpdatedAt(tsUpdated.toLocalDateTime());

        return versao;
    }

    /**
     * Lista todas as versões cadastradas (para geração de cards na tela)
     */
    public List<TGVersao> listarTodas() {
        String sql = "SELECT * FROM tg_versao ORDER BY created_at DESC";
        List<TGVersao> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSetToTGVersao(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todas as versões: " + e.getMessage(), e);
        }

        return lista;
    }

    public List<TGVersao> listarPorSecao(Long secaoId) {
        List<TGVersao> versoes = new ArrayList<>();
        String sql = "SELECT * FROM tg_versao WHERE secao_id = ? ORDER BY numero_versao ASC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, secaoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TGVersao v = new TGVersao();
                v.setId(rs.getLong("id"));
                v.setSecaoId(rs.getLong("secao_id"));
                v.setNumeroVersao(rs.getInt("numero_versao"));
                v.setEmpresa(rs.getString("empresa"));
                v.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                versoes.add(v);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return versoes;
    }

    public Optional<TGVersao> buscarUltimaVersaoPorSecao(long secaoId) {
        String sql = """
            SELECT * FROM tg_versao
            WHERE secao_id = ?
            ORDER BY numero_versao DESC
            LIMIT 1
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, secaoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                TGVersao v = new TGVersao();
                v.setId(rs.getLong("id"));
                v.setSecaoId(rs.getLong("secao_id"));
                v.setNumeroVersao(rs.getInt("numero_versao"));
                v.setSemestre(rs.getString("semestre"));
                v.setAno(rs.getInt("ano"));
                v.setSemestreAno(rs.getString("semestre_ano"));
                return Optional.of(v);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public TGVersao buscarPorSecaoId(long secaoId) {
        String sql = "SELECT * FROM tg_versao WHERE secao_id = ?";
        TGVersao versao = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, secaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    versao = new TGVersao();
                    versao.setSecaoId(rs.getLong("secao_id"));
                    versao.setNumeroVersao(rs.getInt("numero_versao"));
                    versao.setSemestre(rs.getString("semestre"));
                    versao.setAno(rs.getInt("ano"));
                    versao.setSemestreAno(rs.getString("semestre_ano"));
                    versao.setEmpresa(rs.getString("empresa"));
                    versao.setProblema(rs.getString("problema"));
                    versao.setSolucao(rs.getString("solucao"));
                    versao.setRepositorio(rs.getString("repositorio"));
                    versao.setLinkedin(rs.getString("linkedin"));
                    versao.setTecnologias(rs.getString("tecnologias"));
                    versao.setContribuicoes(rs.getString("contribuicoes"));
                    versao.setHardSkills(rs.getString("hard_skills"));
                    versao.setSoftSkills(rs.getString("soft_skills"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar versão: " + e.getMessage(), e);
        }

        return versao;
    }

}
