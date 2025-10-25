package br.com.squadtech.bluetech;

import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    public record SmtpConfig(String host, int port, String user, String pass, boolean starttls, boolean ssl) {}

    private final String from;
    private final SmtpConfig cfg;
    private final Session session;

    public EmailService(String from, SmtpConfig cfg) {
        this.from = from;
        this.cfg = cfg;

        Properties p = new Properties();
        p.put("mail.transport.protocol", "smtp");
        p.put("mail.smtp.host", cfg.host());
        p.put("mail.smtp.port", String.valueOf(cfg.port()));
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.ssl.enable", String.valueOf(cfg.ssl()));
        p.put("mail.smtp.starttls.enable", String.valueOf(cfg.starttls()));

        this.session = Session.getInstance(p, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.user(), cfg.pass());
            }
        });
    }

    public void send(String to, String subject, String body) throws MessagingException {
        if (to == null || to.isBlank()) {
            throw new AddressException("Destinatário vazio");
        }
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");
        Transport.send(msg);
    }
}
