package br.com.squadtech.bluetech.service;

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
     * @param versaoId id da versão (TG_Versao.Id_Versao)
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
            FROM TG_Versao v
            JOIN TG_Secao s 
                 ON (s.Id_Secao = v.Id_Secao) OR (s.Id_Versao = v.Id_Versao)
            JOIN Perfil_Aluno pa  ON pa.email_usuario = s.email_usuario
            LEFT JOIN tg_portifolio tp ON tp.aluno_id = pa.id_perfil_aluno
            JOIN orienta o        ON o.aluno_id = pa.id_perfil_aluno AND o.ativo = TRUE
            JOIN professor p      ON p.id = o.professor_id
            JOIN usuario au       ON au.email = pa.email_usuario
            JOIN usuario pu       ON pu.email = p.usuario_email
            WHERE v.Id_Versao = ?
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

                String assunto = "Novo envio de TG - " + nullSafe(alunoNome);
                String corpo = """
                        Olá, %s!

                        O aluno %s enviou uma nova versão do TG.

                        📘 Título: %s
                        🎯 Tema: %s

                        Acesse o sistema BlueTech para avaliar.

                        — BlueTech
                        """.formatted(nullSafe(profNome), nullSafe(alunoNome), nullSafe(titulo), nullSafe(tema));

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
            JOIN TG_Versao v  ON v.Id_Versao = f.versao_id
            JOIN TG_Secao s   ON (s.Id_Secao = v.Id_Secao) OR (s.Id_Versao = v.Id_Versao)
            JOIN Perfil_Aluno pa ON pa.email_usuario = s.email_usuario
            JOIN usuario au  ON au.email = pa.email_usuario
            JOIN professor p ON p.id = f.professor_id
            JOIN usuario pu  ON pu.email = p.usuario_email
            WHERE f.id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, feedbackId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String alunoNome  = rs.getString("aluno_nome");
                String alunoEmail = rs.getString("aluno_email");
                String profNome   = rs.getString("prof_nome");
                String status     = rs.getString("status");
                String comentario = rs.getString("comentario");

                String assunto = "Feedback do professor - " + nullSafe(profNome);
                String corpo = """
                        Olá, %s!

                        O professor %s enviou um feedback sobre seu TG.

                        📌 Status: %s
                        💬 Comentário: %s

                        Acesse o sistema para ver os detalhes.

                        — BlueTech
                        """.formatted(nullSafe(alunoNome), nullSafe(profNome), nullSafe(status), nullSafe(comentario));

                email.send(alunoEmail, assunto, corpo);
                return alunoEmail; // <- devolve p/ a UI mostrar
            }
        }
    }

    private static String nullSafe(String s) {
        return (s == null) ? "-" : s;
    }
}
