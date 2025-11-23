package br.com.squadtech.bluetech.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConnectionFactory {
    private static final Logger log = LoggerFactory.getLogger(ConnectionFactory.class);
    private static String SERVER_URL;
    private static String DB_URL;
    private static String USER;
    private static String PASSWORD;

    private static HikariDataSource dataSource; // Implementação do Pool (inicializado sob demanda)
    private static volatile boolean dbChecked = false; // garante verificação de criação apenas uma vez

    static {
        try (InputStream input = openDbPropertiesStream()) {
            Properties prop = new Properties();
            if (input == null) {
                log.error("Sorry, unable to find db.properties via classpath or filesystem fallbacks");
                throw new RuntimeException("db.properties not found");
            }
            prop.load(input);
            String dbUrl = prop.getProperty("db.url"); // Full URL including DB name

            // Extract server URL for database creation check (remove database name)
            // Assuming format jdbc:mysql://host:port/dbname
            int lastSlashIndex = dbUrl.lastIndexOf("/");
            if (lastSlashIndex != -1) {
                SERVER_URL = dbUrl.substring(0, lastSlashIndex + 1);
            } else {
                // Fallback if format is unexpected, though this might fail later
                SERVER_URL = dbUrl;
            }

            DB_URL = dbUrl;
            USER = prop.getProperty("db.user");
            PASSWORD = prop.getProperty("db.password");
        } catch (IOException ex) {
            log.error("Error loading db.properties", ex);
            throw new RuntimeException("Error loading db.properties", ex);
        }
    }

    // Inicializa o pool apenas quando for necessário e quando o DB já existir
    private static synchronized HikariDataSource getDataSource() {
        if (dataSource == null) {
            // Garantia adicional: tenta criar o DB antes de iniciar o pool
            if (!dbChecked) {
                try {
                    createDatabaseIfNotExists();
                } catch (RuntimeException e) {
                    log.warn("Não foi possível garantir a criação do database antes do pool: {}", e.getMessage());
                } finally {
                    dbChecked = true;
                }
            }
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setMaximumPoolSize(5); // Tamanho do pool (ajustar conforme ambiente)
            config.setConnectionTimeout(30000); // 30s de timeout
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    // Conexão do pool (para uso normal)
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection(); // Empresta do pool
    }

    // Conexão genérica sem pool (para criação de DB, pois pool precisa do DB
    // existente)
    public static Connection getConnection(String url) throws SQLException {
        return java.sql.DriverManager.getConnection(url, USER, PASSWORD);
    }

    // Cria database (usa conexão sem pool)
    public static void createDatabaseIfNotExists() {
        String sql = "CREATE DATABASE IF NOT EXISTS bluetech";
        try (Connection conn = getConnection(SERVER_URL);
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            log.info("Database 'bluetech' criado ou já existente.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar database: " + e.getMessage());
        }
    }

    // Fecha o pool (chame no shutdown do app, na classe App.java)
    public static void closePool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static InputStream openDbPropertiesStream() {
        InputStream in = null;
        try {
            ClassLoader cl = ConnectionFactory.class.getClassLoader();
            if (cl != null) in = cl.getResourceAsStream("config/db.properties");
            if (in == null) in = ConnectionFactory.class.getResourceAsStream("/config/db.properties");
            if (in == null) {
                ClassLoader tcl = Thread.currentThread().getContextClassLoader();
                if (tcl != null) in = tcl.getResourceAsStream("config/db.properties");
            }
            if (in != null) return in;
            Path[] fallbacks = new Path[] {
                Paths.get("target", "classes", "config", "db.properties"),
                Paths.get("config", "db.properties"),
                Paths.get("src", "main", "resources", "config", "db.properties")
            };
            for (Path path : fallbacks) {
                if (Files.exists(path)) {
                    log.warn("db.properties carregado via caminho alternativo: {}", path.toAbsolutePath());
                    return Files.newInputStream(path);
                }
            }
        } catch (IOException e) {
            log.error("Falha ao tentar localizar db.properties", e);
        }
        return null;
    }
}