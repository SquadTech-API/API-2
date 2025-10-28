package br.com.squadtech.bluetech.config;

public final class SmtpProps {
    private SmtpProps() {}

    // --- ajuste aqui conforme seu provedor (ex: Gmail) ---
    public static final String HOST = "smtp.gmail.com";
    public static final int    PORT = 587;      // Gmail: 587 (STARTTLS) ou 465 (SSL)
    public static final String USER = "bluetechapp.fatec@gmail.com";
    public static final String PASS = "yuel glzy fczh rmxw";
    public static final String FROM = USER;     // remetente padrão
    public static final boolean STARTTLS = true;
    public static final boolean SSL      = false; // se usar 465, troque para true e STARTTLS false
}
