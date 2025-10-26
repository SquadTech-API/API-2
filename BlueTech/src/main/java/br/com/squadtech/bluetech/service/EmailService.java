package br.com.squadtech.bluetech.service;

import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // Configuração SMTP encapsulada como record
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

        // Evita conflito entre SSL e STARTTLS
        if (cfg.ssl()) {
            p.put("mail.smtp.ssl.enable", "true");
            p.put("mail.smtp.starttls.enable", "false");
        } else if (cfg.starttls()) {
            p.put("mail.smtp.ssl.enable", "false");
            p.put("mail.smtp.starttls.enable", "true");
        }

        // Usa Session do jakarta.mail
        this.session = jakarta.mail.Session.getInstance(p, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.user(), cfg.pass());
            }
        });
    }

    /**
     * Envia e-mail em texto puro (text/plain)
     */
    public void send(String to, String subject, String body) throws MessagingException {
        validateRecipient(to);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");

        try {
            Transport.send(msg);
        } catch (AuthenticationFailedException e) {
            throw new MessagingException("Falha de autenticação SMTP: verifique usuário e senha.", e);
        }
    }

    /**
     * Envia e-mail em HTML
     */
    public void sendHtml(String to, String subject, String html) throws MessagingException {
        validateRecipient(to);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject, "UTF-8");
        msg.setContent(html, "text/html; charset=UTF-8");

        try {
            Transport.send(msg);
        } catch (AuthenticationFailedException e) {
            throw new MessagingException("Falha de autenticação SMTP: verifique usuário e senha.", e);
        }
    }

    // Valida destinatário
    private void validateRecipient(String to) throws AddressException {
        if (to == null || to.isBlank()) {
            throw new AddressException("Destinatário vazio");
        }
    }
}
