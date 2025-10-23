package br.com.squadtech.bluetech.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_URL = SERVER_URL + "blue_tech";
    private static final String USER = "usuarioBlueTech";
    private static final String PASSWORD = "BlueTechADM123";

    private static HikariDataSource dataSource; //Implementação do Pool (inicializado sob demanda)

    //Inicializa o pool apenas quando for necessário e quando o DB já existir
    private static synchronized HikariDataSource getDataSource() {
        if (dataSource == null) {
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
        String sql = "CREATE DATABASE IF NOT EXISTS blue_tech";
        try (Connection conn = getConnection(SERVER_URL);
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("Database 'blue_tech' criado ou já existente.");
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