package br.com.squadtech.bluetech.notify;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.config.SmtpProps;
import br.com.squadtech.bluetech.service.EmailService;
import br.com.squadtech.bluetech.service.NotificationService;

import java.sql.Connection;
import java.util.Map;

public final class NotifierFacade {

    private static volatile NotifierFacade INSTANCE;
    private final EmailService email;

    private NotifierFacade() {
        this.email = new EmailService(
                SmtpProps.FROM,
                new EmailService.SmtpConfig(
                        SmtpProps.HOST,
                        SmtpProps.PORT,
                        SmtpProps.USER,
                        SmtpProps.PASS,
                        SmtpProps.STARTTLS,
                        SmtpProps.SSL
                )
        );
    }

    public static NotifierFacade getInstance() {
        if (INSTANCE == null) {
            synchronized (NotifierFacade.class) {
                if (INSTANCE == null) INSTANCE = new NotifierFacade();
            }
        }
        return INSTANCE;
    }

    public static void notify(Object alunoTgAprovado, Map<String, Object> payload) {
    }

    public String notifySubmission(long versaoId) throws Exception {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return new NotificationService(conn, email).notifyProfessorOnStudentSubmission(versaoId);
        }
    }

    public String notifyFeedback(long feedbackId) throws Exception {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return new NotificationService(conn, email).notifyStudentOnProfessorFeedback(feedbackId);
        }
    }
}
