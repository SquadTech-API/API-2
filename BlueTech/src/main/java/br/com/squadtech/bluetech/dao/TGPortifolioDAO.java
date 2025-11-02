package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.TGPortifolio;

import java.sql.*;
import java.math.BigDecimal;

public class TGPortifolioDAO {

    /**
     * Cria a tabela tg_portifolio caso não exista
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS tg_portifolio (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                aluno_id INT NOT NULL,
                titulo VARCHAR(200) NULL,
                tema VARCHAR(200) NULL,
                status ENUM('EM_ANDAMENTO','CONCLUIDO') NOT NULL DEFAULT 'EM_ANDAMENTO',
                percentual_conclusao DECIMAL(5,2) NOT NULL DEFAULT 0.00,
                conteudo_final TEXT NULL,
                data_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                data_conclusao DATETIME NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT uk_tg_aluno UNIQUE (aluno_id),
                CONSTRAINT fk_portifolio_aluno FOREIGN KEY (aluno_id)
                    REFERENCES Perfil_Aluno(id_perfil_aluno) ON DELETE RESTRICT ON UPDATE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela tg_portifolio: " + e.getMessage(), e);
        }
    }

    /**
     * Insere um novo portfólio
     */
    public void insert(TGPortifolio portifolio) {
        String sql = "INSERT INTO tg_portifolio " +
                "(aluno_id, titulo, tema, status, percentual_conclusao, conteudo_final) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, portifolio.getAlunoId());
            stmt.setString(2, portifolio.getTitulo());
            stmt.setString(3, portifolio.getTema());
            stmt.setString(4, portifolio.getStatus());
            stmt.setBigDecimal(5, portifolio.getPercentualConclusao() != null ? portifolio.getPercentualConclusao() : BigDecimal.ZERO);
            stmt.setString(6, portifolio.getConteudoFinal());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    portifolio.setId(generatedKeys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir portifolio: " + e.getMessage(), e);
        }
    }

    /**
     * Busca portfólio pelo ID
     */
    public TGPortifolio findById(Long id) {
        String sql = "SELECT * FROM tg_portifolio WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPortifolio(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar portifolio: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Busca portfólio pelo aluno_id
     */
    public TGPortifolio findByAlunoId(Long alunoId) {
        String sql = "SELECT * FROM tg_portifolio WHERE aluno_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPortifolio(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar portifolio pelo aluno: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Retorna o portfólio do aluno ou cria um novo se não existir
     */
    public Long getOrCreatePortifolioForAluno(Long alunoId) {
        TGPortifolio portifolio = findByAlunoId(alunoId);
        if (portifolio != null) {
            return portifolio.getId();
        }

        TGPortifolio novo = new TGPortifolio();
        novo.setAlunoId(alunoId);
        novo.setStatus("EM_ANDAMENTO");
        insert(novo);

        return novo.getId();
    }

    /**
     * Atualiza o status do portfólio
     */
    public void updateStatus(Long id, String status) {
        String sql = "UPDATE tg_portifolio SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do portifolio: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza o percentual de conclusão do portfólio
     */
    public void updatePercentualConclusao(Long id, BigDecimal percentual) {
        String sql = "UPDATE tg_portifolio SET percentual_conclusao = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, percentual);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar percentual do portifolio: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza o conteúdo final do portfólio e marca a data de conclusão
     */
    public void updateConteudoFinal(Long id, String conteudoFinal) {
        String sql = "UPDATE tg_portifolio SET conteudo_final = ?, data_conclusao = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, conteudoFinal);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar conteudo final do portifolio: " + e.getMessage(), e);
        }
    }

    /**
     * Mapeia um ResultSet para um objeto TGPortifolio
     */
    private TGPortifolio mapResultSetToPortifolio(ResultSet rs) throws SQLException {
        TGPortifolio portifolio = new TGPortifolio();
        portifolio.setId(rs.getLong("id"));
        portifolio.setAlunoId(rs.getLong("aluno_id"));
        portifolio.setTitulo(rs.getString("titulo"));
        portifolio.setTema(rs.getString("tema"));
        portifolio.setStatus(rs.getString("status"));
        portifolio.setPercentualConclusao(rs.getBigDecimal("percentual_conclusao"));
        portifolio.setConteudoFinal(rs.getString("conteudo_final"));
        portifolio.setDataInicio(rs.getTimestamp("data_inicio").toLocalDateTime());

        Timestamp tsDataConclusao = rs.getTimestamp("data_conclusao");
        if (tsDataConclusao != null) {
            portifolio.setDataConclusao(tsDataConclusao.toLocalDateTime());
        }

        portifolio.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        portifolio.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return portifolio;
    }
}
