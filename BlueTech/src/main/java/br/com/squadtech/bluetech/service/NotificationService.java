package br.com.squadtech.bluetech.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

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

    //notificação pro aluno quando o professor aprovar uma versão:

    public String notifyStudentOnVersionApproved(long versaoId) throws Exception {
        final String sql = """
        SELECT 
            au.nome  AS aluno_nome,
            au.email AS aluno_email,
            pu.nome  AS prof_nome,
            tp.titulo AS tg_titulo,
            tp.tema   AS tg_tema,
            v.numero_versao AS versao_numero
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

                String alunoNome  = rs.getString("aluno_nome");
                String alunoEmail = rs.getString("aluno_email");
                String profNome   = rs.getString("prof_nome");
                String titulo     = rs.getString("tg_titulo");
                String tema       = rs.getString("tg_tema");
                int versaoNumero  = rs.getInt("versao_numero");

                String assunto = "Versão " + versaoNumero + " aprovada pelo orientador";
                String corpo = """
                    Olá, %s!

                    Boa notícia! O professor %s aprovou sua versão %d do TG.

                    📘 Título: %s
                    🎯 Tema: %s

                    Continue acompanhando seu progresso no sistema BlueTech.

                    — BlueTech
                    """.formatted(
                        nullSafe(alunoNome),
                        nullSafe(profNome),
                        versaoNumero,
                        nullSafe(titulo),
                        nullSafe(tema)
                );

                email.send(alunoEmail, assunto, corpo);
                return alunoEmail;
            }
        }
    }

    // Notificação para o aluno quando a seção for aprovada

    public String notifyStudentOnSectionApproved(long secaoId) throws Exception {
        final String sql = """
        SELECT 
            au.nome  AS aluno_nome,
            au.email AS aluno_email,
            pu.nome  AS prof_nome,
            tp.titulo AS tg_titulo,
            s.api_numero AS api_numero
        FROM tg_secao s
        JOIN tg_portifolio tp ON s.portifolio_id = tp.id
        JOIN aluno a          ON tp.aluno_id = a.id
        JOIN usuarios au      ON a.usuario_id = au.id
        JOIN professor p      ON tp.professor_id = p.id
        JOIN usuarios pu      ON p.usuario_id = pu.id
        WHERE s.id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, secaoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String alunoNome  = rs.getString("aluno_nome");
                String alunoEmail = rs.getString("aluno_email");
                String profNome   = rs.getString("prof_nome");
                String titulo     = rs.getString("tg_titulo");
                int apiNumero     = rs.getInt("api_numero");

                String assunto = "Seção " + apiNumero + " aprovada - " + nullSafe(titulo);

                String corpo = """
                    Olá, %s!

                    O professor %s aprovou a seção %d do seu Trabalho de Graduação.

                    📘 Título do TG: %s
                    ✅ Seção %d concluída com sucesso!

                    Continue o ótimo trabalho!

                    — BlueTech
                    """.formatted(
                        nullSafe(alunoNome),
                        nullSafe(profNome),
                        apiNumero,
                        nullSafe(titulo),
                        apiNumero
                );

                email.send(alunoEmail, assunto, corpo);
                return alunoEmail; // devolve p/ interface
            }
        }
    }

    //notificação pro aluno quando seu portifolio for aprovado:

    public String notifyStudentOnPortfolioCompleted(long portifolioId) throws Exception {
        final String sql = """
        SELECT 
            au.nome  AS aluno_nome,
            au.email AS aluno_email,
            pu.nome  AS prof_nome,
            tp.titulo AS tg_titulo,
            tp.tema   AS tg_tema
        FROM tg_portifolio tp
        JOIN aluno a           ON tp.aluno_id = a.id
        JOIN usuarios au       ON a.usuario_id = au.id
        JOIN professor p       ON tp.professor_id = p.id
        JOIN usuarios pu       ON p.usuario_id = pu.id
        WHERE tp.id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portifolioId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String alunoNome  = rs.getString("aluno_nome");
                String alunoEmail = rs.getString("aluno_email");
                String profNome   = rs.getString("prof_nome");
                String titulo     = rs.getString("tg_titulo");
                String tema       = rs.getString("tg_tema");

                String assunto = "TG Concluído - " + nullSafe(titulo);

                String corpo = """
                    Olá, %s!

                    Parabéns! Seu Trabalho de Graduação foi concluído com sucesso 🎓

                    📘 Título: %s
                    🎯 Tema: %s
                    👨‍🏫 Orientador: %s

                    Agora você já pode realizar o agendamento da defesa do seu TG.

                    — BlueTech
                    """.formatted(nullSafe(alunoNome), nullSafe(titulo), nullSafe(tema), nullSafe(profNome));

                email.send(alunoEmail, assunto, corpo);
                return alunoEmail;
            }
        }
    }

    // professor cordenador é avisado quando um aluno concluir o portifolio

    public String notifyCoordinatorOnPortfolioCompleted(long portifolioId) throws Exception {
        final String sql = """
        SELECT 
            au.nome  AS aluno_nome,
            au.email AS aluno_email,
            pu.nome  AS prof_nome,
            tp.titulo AS tg_titulo,
            tp.tema   AS tg_tema,
            cu.nome   AS coord_nome,
            cu.email  AS coord_email
        FROM tg_portifolio tp
        JOIN aluno a           ON tp.aluno_id = a.id
        JOIN usuarios au       ON a.usuario_id = au.id
        JOIN professor p       ON tp.professor_id = p.id
        JOIN usuarios pu       ON p.usuario_id = pu.id
        JOIN usuarios cu       ON cu.tipo = 'COORDENADOR' AND cu.ativo = TRUE
        WHERE tp.id = ?
        LIMIT 1
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, portifolioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String alunoNome  = rs.getString("aluno_nome");
                String coordNome  = rs.getString("coord_nome");
                String coordEmail = rs.getString("coord_email");
                String titulo     = rs.getString("tg_titulo");
                String tema       = rs.getString("tg_tema");
                String profNome   = rs.getString("prof_nome");

                String assunto = "Portfólio de TG concluído - " + nullSafe(alunoNome);
                String corpo = """
                    Olá, %s!

                    O aluno %s concluiu seu portfólio de Trabalho de Graduação,
                    sob orientação do professor %s.

                    📘 Título: %s
                    🎯 Tema: %s

                    O trabalho está finalizado e pronto para o agendamento da defesa.

                    — BlueTech
                    """.formatted(
                        nullSafe(coordNome),
                        nullSafe(alunoNome),
                        nullSafe(profNome),
                        nullSafe(titulo),
                        nullSafe(tema)
                );

                email.send(coordEmail, assunto, corpo);
                return coordEmail;
            }
        }
    }

    //notificação para o professor quando o aluno solicita ser orientado por ele

    public String notifyProfessorOnOrientationRequest(long alunoId, long professorId) throws Exception {
        final String sql = """
        SELECT 
            au.nome  AS aluno_nome,
            au.email AS aluno_email,
            pu.nome  AS prof_nome,
            pu.email AS prof_email
        FROM aluno a
        JOIN usuarios au ON a.usuario_id = au.id
        JOIN professor p ON p.id = ?
        JOIN usuarios pu ON p.usuario_id = pu.id
        WHERE a.id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, professorId); // professor
            ps.setLong(2, alunoId);     // aluno

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String alunoNome  = rs.getString("aluno_nome");
                String alunoEmail = rs.getString("aluno_email");
                String profNome   = rs.getString("prof_nome");
                String profEmail  = rs.getString("prof_email");

                String assunto = "Nova solicitação de orientação - " + alunoNome;
                String corpo = """
                    Olá, %s!

                    O aluno %s enviou uma solicitação para que você seja o orientador do Trabalho de Graduação.

                    📩 E-mail do aluno: %s

                    Acesse o sistema BlueTech para aprovar ou recusar a solicitação.

                    — BlueTech
                    """.formatted(nullSafe(profNome), nullSafe(alunoNome), nullSafe(alunoEmail));

                email.send(profEmail, assunto, corpo);
                return profEmail;
            }
        }
    }

    //resposta pro aluno se o professor aceitou ou não a solicitação dele de orientação

    public String notifyStudentOnOrientationResponse(long alunoId, long professorId, boolean aceita) throws Exception {
        final String sql = """
        SELECT 
            au.nome  AS aluno_nome,
            au.email AS aluno_email,
            pu.nome  AS prof_nome
        FROM aluno a
        JOIN usuarios au ON a.usuario_id = au.id
        JOIN professor p ON p.id = ?
        JOIN usuarios pu ON p.usuario_id = pu.id
        WHERE a.id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, professorId);
            ps.setLong(2, alunoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String alunoNome  = rs.getString("aluno_nome");
                String alunoEmail = rs.getString("aluno_email");
                String profNome   = rs.getString("prof_nome");

                String assunto = "Resposta à solicitação de orientação — " + nullSafe(profNome);

                String corpo = """
                    Olá, %s!

                    O professor %s %s sua solicitação de orientação para o Trabalho de Graduação.

                    %s

                    — BlueTech
                    """
                        .formatted(
                                nullSafe(alunoNome),
                                nullSafe(profNome),
                                aceita ? "aceitou" : "recusou",
                                aceita
                                        ? "Agora você está oficialmente vinculado a este orientador no sistema."
                                        : "Você poderá escolher outro orientador disponível."
                        );

                email.send(alunoEmail, assunto, corpo);
                return alunoEmail;
            }
        }
    }

    //notificação do agendamento da defesa

    public void notifyOnDefenseScheduled(long defesaId) throws Exception {
        final String sql = """
        SELECT 
            d.id AS defesa_id,
            d.data_hora,
            d.sala,
            tp.titulo AS tg_titulo,
            tp.tema AS tg_tema,
            au.nome AS aluno_nome,
            au.email AS aluno_email,
            pu.nome AS professor_nome,
            pu.email AS professor_email
        FROM defesa d
        JOIN tg_portifolio tp ON d.portifolio_id = tp.id
        JOIN aluno a          ON tp.aluno_id = a.id
        JOIN usuarios au      ON a.usuario_id = au.id
        JOIN professor p      ON tp.professor_id = p.id
        JOIN usuarios pu      ON p.usuario_id = pu.id
        WHERE d.id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, defesaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return;

                String alunoNome      = rs.getString("aluno_nome");
                String alunoEmail     = rs.getString("aluno_email");
                String professorNome  = rs.getString("professor_nome");
                String professorEmail = rs.getString("professor_email");
                String titulo         = rs.getString("tg_titulo");
                String tema           = rs.getString("tg_tema");
                Timestamp dataHora    = rs.getTimestamp("data_hora");
                String sala           = rs.getString("sala");

                String dataFormatada = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                        .format(dataHora);

                // --- E-mail para o aluno ---
                String assuntoAluno = "Defesa do seu TG agendada!";
                String corpoAluno = """
                Olá, %s!

                Sua defesa de Trabalho de Graduação foi agendada com sucesso.

                📘 Título: %s
                🎯 Tema: %s
                🗓️ Data e horário: %s
                📍 Sala: %s
                👨‍🏫 Orientador: %s

                Prepare-se bem e boa sorte!

                — BlueTech
                """.formatted(
                        nullSafe(alunoNome),
                        nullSafe(titulo),
                        nullSafe(tema),
                        dataFormatada,
                        nullSafe(sala),
                        nullSafe(professorNome)
                );

                email.send(alunoEmail, assuntoAluno, corpoAluno);

                // --- E-mail para o professor ---
                String assuntoProfessor = "Defesa agendada para seu orientando";
                String corpoProfessor = """
                Olá, %s!

                A defesa do seu orientando %s foi agendada.

                📘 Título: %s
                🎯 Tema: %s
                🗓️ Data e horário: %s
                📍 Sala: %s

                — BlueTech
                """.formatted(
                        nullSafe(professorNome),
                        nullSafe(alunoNome),
                        nullSafe(titulo),
                        nullSafe(tema),
                        dataFormatada,
                        nullSafe(sala)
                );

                email.send(professorEmail, assuntoProfessor, corpoProfessor);
            }
        }
    }






    private static String nullSafe(String s) {
        return (s == null) ? "-" : s;
    }
}
