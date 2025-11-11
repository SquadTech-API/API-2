package br.com.squadtech.bluetech.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {
    private static final Logger log = LoggerFactory.getLogger(ConnectionFactory.class);
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_URL = SERVER_URL + "bluetech";
    private static final String USER = "bluetech";
    private static final String PASSWORD = "BlueTechADM123";

    private static HikariDataSource dataSource; //Implementação do Pool (inicializado sob demanda)
    private static volatile boolean dbChecked = false; // garante verificação de criação apenas uma vez

    //Inicializa o pool apenas quando for necessário e quando o DB já existir
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
            config.setMaximumPoolSize(5); //Tamanho do pool (ajustar conforme ambiente)
            config.setConnectionTimeout(30000); //30s de timeout
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    //Conexão do pool (para uso normal)
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection(); // Empresta do pool
    }

    //Conexão genérica sem pool (para criação de DB, pois pool precisa do DB existente)
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

    //Fecha o pool (chame no shutdown do app, na classe App.java)
    public static void closePool() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}