package br.com.squadtech.bluetech.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import br.com.squadtech.bluetech.model.FeedbackItem;
import br.com.squadtech.bluetech.notify.NotifierFacade;
import br.com.squadtech.bluetech.dao.NotifierEvents;
import br.com.squadtech.bluetech.viewmodel.FeedbackHistoricoVM;
import java.sql.*;
import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Feedback;

import java.util.ArrayList;



import java.sql.*;

public class FeedbackDAO {

    private final FeedbackItemDAO itemDAO = new FeedbackItemDAO();

    /**
     * Cria a tabela feedback se não existir
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS feedback (
                id INT AUTO_INCREMENT PRIMARY KEY,
                versao_id INT NOT NULL,
                professor_id BIGINT NOT NULL,
                status ENUM('APROVADO','AJUSTES') NOT NULL,
                comentario TEXT NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT fk_feedback_versao FOREIGN KEY (versao_id)
                    REFERENCES TG_Versao(Id_Versao) ON DELETE CASCADE ON UPDATE CASCADE,
                CONSTRAINT fk_feedback_professor FOREIGN KEY (professor_id)
                    REFERENCES professor(id) ON DELETE RESTRICT ON UPDATE CASCADE,
                INDEX idx_feedback_versao (versao_id),
                INDEX idx_feedback_professor (professor_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            createFeedbackItemTableIfNotExists(conn); // Chama a criação da nova tabela
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Cria a tabela feedback_item se não existir
     */
    private void createFeedbackItemTableIfNotExists(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS feedback_item (
                id INT AUTO_INCREMENT PRIMARY KEY,
                feedback_id INT NOT NULL,
                campo VARCHAR(255) NOT NULL,
                status ENUM('OK','AJUSTE') NOT NULL,
                comentario TEXT NULL,
                CONSTRAINT fk_feedback_item_feedback FOREIGN KEY (feedback_id)
                    REFERENCES feedback(id) ON DELETE CASCADE ON UPDATE CASCADE,
                INDEX idx_feedback_item_feedback (feedback_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Busca feedback existente pelo ID da versão
     */
    public Feedback buscarPorSecaoId(long versaoId) {
        String sql = """
        SELECT *
        FROM feedback
        WHERE versao_id = ?
        ORDER BY GREATEST(COALESCE(updated_at, created_at), created_at) DESC, id DESC
        LIMIT 1
    """;
        Feedback feedback = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, versaoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    feedback = new Feedback();
                    feedback.setId(rs.getInt("id"));
                    feedback.setVersaoId(rs.getInt("versao_id"));
                    feedback.setProfessorId(rs.getInt("professor_id"));
                    feedback.setStatus(rs.getString("status"));
                    feedback.setComentario(rs.getString("comentario"));

                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) feedback.setCreatedAt(created.toLocalDateTime());
                    Timestamp updated = rs.getTimestamp("updated_at");
                    if (updated != null) feedback.setUpdatedAt(updated.toLocalDateTime());

                    // carrega itens do feedback MAIS RECENTE
                    feedback.setItens(itemDAO.findByFeedbackId(feedback.getId()));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar feedback pela versão: " + e.getMessage(), e);
        }

        return feedback;
    }


    // Alias com nome mais preciso
    public Feedback buscarPorVersaoId(long versaoId) {
        return buscarPorSecaoId(versaoId);
    }

    /**
     * Insere ou atualiza feedback e retorna o ID do registro afetado
     */
    /**
     * Insere ou atualiza feedback e seus itens, e retorna o ID do registro afetado.
     * O status é determinado pela existência de itens com status 'AJUSTE'.
     */
    public Long salvarFeedbackReturnId(long versaoId, long professorId, String comentario, List<FeedbackItem> itens) {
        // 1. Determinar o status geral
        String status = itens.stream().anyMatch(item -> "AJUSTE".equals(item.getStatus())) ? "AJUSTES" : "APROVADO";

        Feedback existente = buscarPorSecaoId(versaoId);
        Long feedbackId = null;

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false); // Inicia transação

            if (existente == null) {
                // 2. Inserir Feedback principal
                String sql = "INSERT INTO feedback (versao_id, professor_id, comentario, status) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setLong(1, versaoId);
                    ps.setLong(2, professorId);
                    ps.setString(3, comentario);
                    ps.setString(4, status);

                    int linhas = ps.executeUpdate();
                    if (linhas > 0) {
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                feedbackId = rs.getLong(1);
                            }
                        }
                    }
                }
            } else {
                // 2. Atualizar Feedback principal
                feedbackId = Long.valueOf(existente.getId());
                String sql = "UPDATE feedback SET comentario = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, comentario);
                    ps.setString(2, status);
                    ps.setLong(3, feedbackId);

                    ps.executeUpdate();
                }
                // 3. Deletar itens antigos antes de inserir os novos (para atualização)
                String deleteSql = "DELETE FROM feedback_item WHERE feedback_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setLong(1, feedbackId);
                    ps.executeUpdate();
                }
            }

            if (feedbackId != null) {
                // 4. Inserir novos FeedbackItems
                for (FeedbackItem item : itens) {
                    item.setFeedbackId(feedbackId.intValue());
                    itemDAO.save(item, conn); // Usar a conexão da transação
                }
            }

            conn.commit(); // Confirma transação
            return feedbackId;

        } catch (SQLException e) {
            e.printStackTrace();
            // Rollback em caso de erro (não implementado aqui, mas idealmente deveria estar)
            return null;
        }
    }

    /**
     * Variante que também enfileira notificação ao aluno após salvar/atualizar o feedback.
     */
    public Long salvarFeedbackReturnIdAndNotify(long versaoId,
                                                long professorId,
                                                String comentarioGeral,
                                                List<FeedbackItem> itens) {
        String insertFeedbackSql = """
            INSERT INTO feedback (versao_id, professor_id, status, comentario, created_at)
            VALUES (?, ?, ?, ?, NOW())
        """;

        String insertItemSql = """
            INSERT INTO feedback_item (feedback_id, campo, status, comentario)
            VALUES (?, ?, ?, ?)
        """;

        String selectSecaoSql = """
            SELECT s.Id_Secao, s.email_usuario AS aluno_email, u.nome AS aluno_nome,
                   v.Versao_Numero AS versao_numero, s.API_Numero AS api_numero
            FROM tg_versao v
            JOIN tg_secao s ON s.Id_Secao = v.Id_Secao
            JOIN usuario u   ON u.email = s.email_usuario
            WHERE v.Id_Versao = ?
            FOR UPDATE
        """;

        String updateStatusSecaoSql = "UPDATE tg_secao SET Status = ? WHERE Id_Secao = ?";

        Connection conn = null;
        PreparedStatement psFeedback = null;
        PreparedStatement psItem = null;
        PreparedStatement psSel = null;
        PreparedStatement psUpd = null;
        ResultSet rs = null;

        try {
            // 1) status final com base nos itens
            boolean temAjuste = itens.stream().anyMatch(it -> "AJUSTE".equalsIgnoreCase(it.getStatus()));
            String statusFinal = temAjuste ? "AJUSTES" : "APROVADO";

            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            // 2) descobre seção + aluno (lock otimista na linha)
            psSel = conn.prepareStatement(selectSecaoSql);
            psSel.setLong(1, versaoId);
            rs = psSel.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Seção/Aluno não encontrados para a versão " + versaoId);
            }
            long secaoId = rs.getLong("Id_Secao");
            String alunoEmail = rs.getString("aluno_email");
            String alunoNome = rs.getString("aluno_nome");
            int versaoNumero  = rs.getInt("versao_numero");
            int apiNumero     = rs.getInt("api_numero");

            // 3) insere feedback
            psFeedback = conn.prepareStatement(insertFeedbackSql, Statement.RETURN_GENERATED_KEYS);
            psFeedback.setLong(1, versaoId);
            psFeedback.setLong(2, professorId);
            psFeedback.setString(3, statusFinal);
            psFeedback.setString(4, comentarioGeral);
            psFeedback.executeUpdate();

            Long feedbackId;
            try (ResultSet gen = psFeedback.getGeneratedKeys()) {
                if (gen.next()) feedbackId = gen.getLong(1);
                else throw new SQLException("Falha ao obter ID do feedback.");
            }

            // 4) insere itens
            psItem = conn.prepareStatement(insertItemSql);
            for (FeedbackItem it : itens) {
                psItem.setLong(1, feedbackId);
                psItem.setString(2, it.getCampo());
                psItem.setString(3, it.getStatus());
                psItem.setString(4, it.getComentario());
                psItem.addBatch();
            }
            psItem.executeBatch();

            // 5) atualiza status na TG_Secao
            psUpd = conn.prepareStatement(updateStatusSecaoSql);
            psUpd.setString(1, statusFinal);
            psUpd.setLong(2, secaoId);
            psUpd.executeUpdate();

            conn.commit();

            // 6) notifica o aluno (fora da transação)
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("alunoNome", alunoNome);
                payload.put("alunoEmail", alunoEmail);
                payload.put("status", statusFinal);
                payload.put("comentarioGeral", comentarioGeral);
                payload.put("versaoNumero", versaoNumero);
                payload.put("apiNumero", apiNumero);
                payload.put("versaoId", versaoId);
                payload.put("feedbackId", feedbackId);

                if ("AJUSTES".equals(statusFinal)) {
                    NotifierFacade.notify(NotifierEvents.ALUNO_TG_AJUSTES, payload);
                } else {
                    NotifierFacade.notify(NotifierEvents.ALUNO_TG_APROVADO, payload);
                }
            } catch (Exception notifyEx) {
                // não derruba o fluxo se o e-mail falhar — logue e siga
                System.err.println("Falha ao notificar aluno: " + notifyEx.getMessage());
            }

            return feedbackId;

        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException("Erro ao salvar feedback/atualizar status: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (psSel != null) psSel.close(); } catch (Exception ignore) {}
            try { if (psItem != null) psItem.close(); } catch (Exception ignore) {}
            try { if (psFeedback != null) psFeedback.close(); } catch (Exception ignore) {}
            try { if (psUpd != null) psUpd.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    /**
     * Variante que também enfileira notificação ao aluno após salvar/atualizar o feedback.
     */
    // O método salvarFeedbackReturnIdAndNotify foi movido e adaptado acima. Removendo o antigo.

    /**
     * Busca o histórico de feedbacks para uma determinada versão.
     * Como o feedback atualiza o registro existente, esta função deve ser revisada
     * se o requisito for histórico de *todas* as alterações.
     *
     * Pelo requisito, o histórico é de feedbacks anteriores. O modelo atual
     * sugere que há apenas um feedback por versao_id.
     *
     * Se o requisito for histórico de feedbacks para *todas* as versões de uma seção,
     * o método deve ser alterado para buscar por secao_id.
     *
     * Assumindo que o histórico é de feedbacks de *outras* versões da mesma seção (TGSecao).
     */
    public List<Feedback> buscarHistoricoPorSecaoId(long secaoId) {
        String sql = """
            SELECT f.* FROM feedback f
            JOIN TG_Versao v ON f.versao_id = v.Id_Versao
            WHERE v.Id_Secao = ?
            ORDER BY f.created_at DESC
        """;
        List<Feedback> historico = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, secaoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Feedback feedback = new Feedback();
                    feedback.setId(rs.getInt("id"));
                    feedback.setVersaoId(rs.getInt("versao_id"));
                    feedback.setProfessorId(rs.getInt("professor_id"));
                    feedback.setStatus(rs.getString("status"));
                    feedback.setComentario(rs.getString("comentario"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) feedback.setCreatedAt(created.toLocalDateTime());
                    Timestamp updated = rs.getTimestamp("updated_at");
                    if (updated != null) feedback.setUpdatedAt(updated.toLocalDateTime());
                    
                    // Carrega os itens de feedback para o histórico
                    feedback.setItens(itemDAO.findByFeedbackId(feedback.getId()));
                    historico.add(feedback);
                }           }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar histórico de feedback pela seção: " + e.getMessage(), e);
        }

        return historico;
    }
    public List<FeedbackHistoricoVM> listarHistoricoPorSecaoId(long secaoId) {
        String sql = """
            SELECT 
                f.id AS feedback_id,
                f.versao_id,
                v.Versao_Numero AS versao_numero,
                s.API_Numero    AS api_numero,
                f.status,
                f.comentario,
                f.created_at,
                u.nome AS professor_nome
            FROM feedback f
            JOIN tg_versao v  ON v.Id_Versao = f.versao_id
            JOIN tg_secao  s  ON s.Id_Secao  = v.Id_Secao
            JOIN professor p  ON p.id        = f.professor_id
            JOIN usuario   u  ON u.email     = p.usuario_email
            WHERE s.Id_Secao = ?
            ORDER BY f.created_at DESC
        """;

        List<FeedbackHistoricoVM> list = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, secaoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapHistorico(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar histórico por seção: " + e.getMessage(), e);
        }
        return list;
    }

    public List<FeedbackHistoricoVM> listarHistoricoPorAlunoEmail(String alunoEmail) {
        String sql = """
            SELECT 
                f.id AS feedback_id,
                f.versao_id,
                v.Versao_Numero AS versao_numero,
                s.API_Numero    AS api_numero,
                f.status,
                f.comentario,
                f.created_at,
                u.nome AS professor_nome
            FROM feedback f
            JOIN tg_versao v  ON v.Id_Versao = f.versao_id
            JOIN tg_secao  s  ON s.Id_Secao  = v.Id_Secao
            JOIN professor p  ON p.id        = f.professor_id
            JOIN usuario   u  ON u.email     = p.usuario_email
            WHERE s.email_usuario = ?
            ORDER BY f.created_at DESC
        """;

        List<FeedbackHistoricoVM> list = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, alunoEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapHistorico(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar histórico por aluno: " + e.getMessage(), e);
        }
        return list;
    }

    private FeedbackHistoricoVM mapHistorico(ResultSet rs) throws SQLException {
        FeedbackHistoricoVM vm = new FeedbackHistoricoVM();
        vm.setId(rs.getLong("feedback_id"));
        vm.setVersaoId(rs.getLong("versao_id"));
        vm.setVersaoNumero(rs.getInt("versao_numero"));
        vm.setApiNumero(rs.getInt("api_numero"));
        vm.setStatus(rs.getString("status"));
        vm.setComentarioGeral(rs.getString("comentario"));
        Timestamp ts = rs.getTimestamp("created_at");
        vm.setCriadoEm(ts != null ? ts.toLocalDateTime() : null);
        vm.setProfessorNome(rs.getString("professor_nome"));
        return vm;
    }
    public List<FeedbackItem> listarItensPorFeedbackId(Long feedbackId) {
        String sql = """
        SELECT id, campo, status, comentario
        FROM feedback_item
        WHERE feedback_id = ?
        ORDER BY id ASC
    """;
        List<FeedbackItem> itens = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, feedbackId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FeedbackItem it = new FeedbackItem(
                            (int) rs.getLong("id"),
                            rs.getString("campo"),
                            rs.getString("status"),
                            rs.getString("comentario")
                    );
                    itens.add(it);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar itens do feedback " + feedbackId + ": " + e.getMessage(), e);
        }
        return itens;
    }

}

