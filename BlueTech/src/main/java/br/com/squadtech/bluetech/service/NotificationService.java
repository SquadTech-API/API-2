package br.com.squadtech.bluetech.service;

import br.com.squadtech.bluetech.EmailService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Envia e-mails transacionais e RETORNA o destinatário usado,
 * para que a UI possa mostrar "Enviado para: <email>".
 */
public class NotificationService {

    private final Connection conn;
    private final EmailService email;

    public NotificationService(Connection conn, EmailService email) {
        this.conn = conn;
        this.email = email;
    }

    /**
     * Quando o aluno envia uma versão do TG, avisa o professor orientador.
     * @param versaoId id da versão (tg_versao.id)
     * @return e-mail do professor destinatário (ou null, se não achou registro)
     */
    public String notifyProfessorOnStudentSubmission(long versaoId) throws Exception {
        final String sql = """
            SELECT 
                au.nome  AS aluno_nome,
                au.email AS aluno_email,
                pu.nome  AS prof_nome,
                pu.email AS prof_email,
                tp.titulo AS tg_titulo,
                tp.tema   AS tg_tema
            FROM tg_versao v
            JOIN tg_secao s        ON v.secao_id = s.id
            JOIN tg_portifolio tp  ON s.portifolio_id = tp.id
            JOIN aluno a           ON tp.aluno_id = a.id
            JOIN usuarios au       ON a.usuario_id = au.id
            JOIN professor p       ON tp.professor_id = p.id
            JOIN usuarios pu       ON p.usuario_id = pu.id
            WHERE v.id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, versaoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String alunoNome = rs.getString("aluno_nome");
                String profNome  = rs.getString("prof_nome");
                String profEmail = rs.getString("prof_email");
                String titulo    = rs.getString("tg_titulo");
                String tema      = rs.getString("tg_tema");

                String assunto = "Novo envio de TG - " + alunoNome;
                String corpo = """
                        Olá, %s!

                        O aluno %s enviou uma nova versão do TG.

                        📘 Título: %s
                        🎯 Tema: %s

                        Acesse o sistema BlueTech para avaliar.

                        — BlueTech
                        """.formatted(profNome, alunoNome, nullSafe(titulo), nullSafe(tema));

                email.send(profEmail, assunto, corpo);
                return profEmail; // <- devolve p/ a UI mostrar
            }
        }
    }

    /**
     * Quando o professor envia um feedback, avisa o aluno.
     * @param feedbackId id do feedback (feedback.id)
     * @return e-mail do aluno destinatário (ou null, se não achou registro)
     */
    public String notifyStudentOnProfessorFeedback(long feedbackId) throws Exception {
        final String sql = """
            SELECT 
                au.nome  AS aluno_nome,
                au.email AS aluno_email,
                pu.nome  AS prof_nome,
                f.status AS status,
                f.comentario AS comentario
            FROM feedback f
            JOIN tg_versao v       ON f.versao_id = v.id
            JOIN tg_secao s        ON v.secao_id = s.id
            JOIN tg_portifolio tp  ON s.portifolio_id = tp.id
            JOIN aluno a           ON tp.aluno_id = a.id
            JOIN usuarios au       ON a.usuario_id = au.id
            JOIN professor p       ON tp.professor_id = p.id
            JOIN usuarios pu       ON p.usuario_id = pu.id
            WHERE f.id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, feedbackId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String alunoNome  = rs.getString("aluno_nome");
                String alunoEmail = rs.getString("aluno_email"); // <- corrigido (era resultadoDoBanco)
                String profNome   = rs.getString("prof_nome");
                String status     = rs.getString("status");
                String comentario = rs.getString("comentario");

                String assunto = "Feedback do professor - " + profNome;
                String corpo = """
                        Olá, %s!

                        O professor %s enviou um feedback sobre seu TG.

                        📌 Status: %s
                        💬 Comentário: %s

                        Acesse o sistema para ver os detalhes.

                        — BlueTech
                        """.formatted(alunoNome, profNome, nullSafe(status), nullSafe(comentario));

                email.send(alunoEmail, assunto, corpo);
                return alunoEmail; // <- devolve p/ a UI mostrar
            }
        }
    }

    private static String nullSafe(String s) {
        return (s == null) ? "-" : s;
    }
}
