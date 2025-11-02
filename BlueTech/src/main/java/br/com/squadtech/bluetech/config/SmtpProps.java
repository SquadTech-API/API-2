package br.com.squadtech.bluetech.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public final class SmtpProps {
    private static final Logger log = LoggerFactory.getLogger(SmtpProps.class);
    private SmtpProps() {}

    private static final Properties FILE_PROPS = new Properties();

    static {
        // Try to load properties from classpath: /config/smtp.properties
        try (InputStream in = SmtpProps.class.getResourceAsStream("/config/smtp.properties")) {
            if (in != null) {
                FILE_PROPS.load(in);
                log.info("SMTP properties loaded from classpath: /config/smtp.properties");
            } else {
                log.info("No smtp.properties found on classpath; using env/system defaults.");
            }
        } catch (Exception e) {
            log.warn("Failed to load /config/smtp.properties: {}", e.toString());
        }
    }

    private static String env(String key, String def) {
        // precedence: file props -> env -> system property -> default
        String v = FILE_PROPS.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key);
        if (v == null || v.isBlank()) v = System.getProperty(key);
        if (v == null || v.isBlank()) v = def;
        return v;
    }
    private static int envInt(String key, int def) {
        try { return Integer.parseInt(env(key, Integer.toString(def)).trim()); } catch (Exception e) { return def; }
    }
    private static boolean envBool(String key, boolean def) {
        String v = env(key, null);
        if (v == null) return def;
        return switch (v.trim().toLowerCase()) {
            case "1", "true", "yes", "y", "on" -> true;
            case "0", "false", "no", "n", "off" -> false;
            default -> def;
        };
    }

    public static final String HOST = env("SMTP_HOST", "smtp.gmail.com");
    public static final int    PORT = envInt("SMTP_PORT", 587);
    public static final String USER = env("SMTP_USER", "");
    public static final String PASS = env("SMTP_PASS", "");
    public static final String FROM = env("SMTP_FROM", USER);
    public static final boolean STARTTLS = envBool("SMTP_STARTTLS", true);
    public static final boolean SSL      = envBool("SMTP_SSL", false);

    static {
        if (USER == null || USER.isBlank()) {
            log.warn("SMTP_USER is not set; emails may fail to send.");
        }
        if (PASS == null || PASS.isBlank()) {
            log.warn("SMTP_PASS is not set; emails may fail to send.");
        }
        if (SSL && STARTTLS) {
            log.warn("Both SMTP_SSL and SMTP_STARTTLS appear enabled. Prefer one: SSL for 465, STARTTLS for 587. Disabling STARTTLS.");
        }
    }
}
