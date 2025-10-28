package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.TGSecao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TGSecaoDAO {

    /**
     * Cria a tabela tg_secao caso não exista
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS tg_secao (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                portifolio_id BIGINT NOT NULL,
                api_numero TINYINT NOT NULL CHECK (api_numero BETWEEN 1 AND 6),
                status ENUM('PENDENTE','CONCLUIDA') NOT NULL DEFAULT 'PENDENTE',
                versao_validada BOOLEAN NOT NULL DEFAULT FALSE,
                data_validacao DATETIME NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT uk_portifolio_api UNIQUE (portifolio_id, api_numero),
                CONSTRAINT fk_secao_portifolio FOREIGN KEY (portifolio_id)
                    REFERENCES tg_portifolio(id) ON DELETE CASCADE ON UPDATE CASCADE,
                INDEX idx_secao_portifolio (portifolio_id, api_numero)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela tg_secao: " + e.getMessage(), e);
        }
    }

    /**
     * Insere uma nova seção e retorna o ID gerado
     */
    public Long insertReturningId(TGSecao secao) {
        if (secao == null || secao.getPortifolioId() == null) {
            throw new IllegalArgumentException("Seção ou portifolioId não podem ser nulos");
        }

        String sql = "INSERT INTO tg_secao (portifolio_id, api_numero, status, versao_validada, data_validacao) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, secao.getPortifolioId());
            stmt.setInt(2, secao.getApiNumero());
            stmt.setString(3, secao.getStatus() != null ? secao.getStatus() : "PENDENTE");
            stmt.setBoolean(4, secao.getVersaoValidada() != null && secao.getVersaoValidada());
            if (secao.getDataValidacao() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(secao.getDataValidacao()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    secao.setId(id);
                    return id;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir seção: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Busca seção pelo ID
     */
    public TGSecao findById(Long id) {
        if (id == null) return null;

        String sql = "SELECT * FROM tg_secao WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTGSecao(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar seção: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Busca seção pelo api_numero e portifolio_id
     */
    public TGSecao findByApiNumeroAndPortifolio(int apiNumero, Long portifolioId) {
        if (portifolioId == null) return null;

        String sql = "SELECT * FROM tg_secao WHERE api_numero = ? AND portifolio_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apiNumero);
            stmt.setLong(2, portifolioId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTGSecao(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar seção por api_numero: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Faz insert ou update (upsert) baseado em portifolio_id e api_numero
     * Retorna o ID da seção criada ou atualizada
     */
    public Long upsertByApiNumero(TGSecao secao) {
        if (secao == null || secao.getPortifolioId() == null) {
            throw new IllegalArgumentException("Seção ou portifolioId não podem ser nulos");
        }

        String sql = """
            INSERT INTO tg_secao (portifolio_id, api_numero, status, versao_validada, data_validacao)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                status = VALUES(status),
                versao_validada = VALUES(versao_validada),
                data_validacao = VALUES(data_validacao),
                updated_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, secao.getPortifolioId());
            stmt.setInt(2, secao.getApiNumero());
            stmt.setString(3, secao.getStatus() != null ? secao.getStatus() : "PENDENTE");
            stmt.setBoolean(4, secao.getVersaoValidada() != null && secao.getVersaoValidada());
            stmt.setTimestamp(5, secao.getDataValidacao() != null ? Timestamp.valueOf(secao.getDataValidacao()) : null);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    secao.setId(id);
                    return id;
                }
            }

            // Caso já exista, buscar o ID
            TGSecao existente = findByApiNumeroAndPortifolio(secao.getApiNumero(), secao.getPortifolioId());
            if (existente != null) {
                secao.setId(existente.getId());
                return existente.getId();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao realizar upsert em tg_secao: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Atualiza status da seção
     */
    public void updateStatus(Long id, String status) {
        if (id == null || status == null) return;

        String sql = "UPDATE tg_secao SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setLong(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status da seção: " + e.getMessage(), e);
        }
    }

    /**
     * Valida a seção (marca versao_validada e data_validacao)
     */
    public void validateSecao(Long id, boolean versaoValidada) {
        if (id == null) return;

        String sql = "UPDATE tg_secao SET versao_validada = ?, data_validacao = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, versaoValidada);
            stmt.setLong(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao validar seção: " + e.getMessage(), e);
        }
    }

    /**
     * Lista todas as seções de um portfólio
     */
    public List<TGSecao> findByPortifolioId(Long portifolioId) {
        List<TGSecao> lista = new ArrayList<>();
        if (portifolioId == null) return lista;

        String sql = "SELECT * FROM tg_secao WHERE portifolio_id = ? ORDER BY api_numero";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, portifolioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToTGSecao(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar seções do portifolio: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Lista todas as seções
     */
    public List<TGSecao> findAll() {
        List<TGSecao> lista = new ArrayList<>();
        String sql = "SELECT * FROM tg_secao ORDER BY api_numero";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSetToTGSecao(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todas as seções: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Mapeia ResultSet para objeto TGSecao
     */
    private TGSecao mapResultSetToTGSecao(ResultSet rs) throws SQLException {
        TGSecao secao = new TGSecao();

        secao.setId(rs.getLong("id"));
        secao.setPortifolioId(rs.getLong("portifolio_id"));
        secao.setApiNumero(rs.getInt("api_numero"));
        secao.setStatus(rs.getString("status"));
        secao.setVersaoValidada(rs.getBoolean("versao_validada"));

        Timestamp tsDataValidacao = rs.getTimestamp("data_validacao");
        if (tsDataValidacao != null) {
            secao.setDataValidacao(tsDataValidacao.toLocalDateTime());
        }

        Timestamp tsCreated = rs.getTimestamp("created_at");
        if (tsCreated != null) {
            secao.setCreatedAt(tsCreated.toLocalDateTime());
        }

        Timestamp tsUpdated = rs.getTimestamp("updated_at");
        if (tsUpdated != null) {
            secao.setUpdatedAt(tsUpdated.toLocalDateTime());
        }

        return secao;
    }

    public List<TGSecao> findByAlunoId(Long alunoId) {
        String sql = """
        SELECT s.* 
        FROM tg_secao s
        JOIN tg_portifolio p ON s.portifolio_id = p.id
        WHERE p.aluno_id = ?
        ORDER BY s.api_numero
    """;

        List<TGSecao> secoes = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, alunoId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TGSecao s = new TGSecao();
                s.setId(rs.getLong("id"));
                s.setPortifolioId(rs.getLong("portifolio_id"));
                s.setApiNumero(rs.getInt("api_numero"));
                s.setStatus(rs.getString("status"));
                s.setVersaoValidada(rs.getBoolean("versao_validada"));
                s.setDataValidacao(rs.getTimestamp("data_validacao") != null
                        ? rs.getTimestamp("data_validacao").toLocalDateTime()
                        : null);
                s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                secoes.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return secoes;
    }
}
