package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.TGSecao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TGSecaoDAO {

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS TG_Secao (" +
                "Id_Secao INT AUTO_INCREMENT PRIMARY KEY," +
                "API_Numero INT NOT NULL," +
                "Data_Envio DATETIME NOT NULL," +
                "Data_Aprovacao DATETIME NULL," +
                "Status VARCHAR(50) NOT NULL," +
                "Id_Versao INT NOT NULL," +
                "CONSTRAINT fk_tgsecao_versao FOREIGN KEY (Id_Versao) REFERENCES TG_Versao(Id_Versao)" +
                ")";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela TG_Secao: " + e.getMessage(), e);
        }
    }

    /**
     * Upsert simples por API_Numero: se existir, atualiza para nova versao; senão, insere.
     * Considera Status inicial como "Em andamento" se não fornecido.
     */
    public void upsertByApiNumero(TGSecao secao) {
        String select = "SELECT Id_Secao FROM TG_Secao WHERE API_Numero = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement psSelect = conn.prepareStatement(select)) {

            psSelect.setInt(1, secao.getApiNumero());
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    int idSecao = rs.getInt("Id_Secao");
                    String update = "UPDATE TG_Secao SET Data_Envio = ?, Status = ?, Id_Versao = ? WHERE Id_Secao = ?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(update)) {
                        psUpdate.setTimestamp(1, Timestamp.valueOf(secao.getDataEnvio()));
                        psUpdate.setString(2, secao.getStatus() == null ? "Em andamento" : secao.getStatus());
                        psUpdate.setInt(3, secao.getIdVersao());
                        psUpdate.setInt(4, idSecao);
                        psUpdate.executeUpdate();
                    }
                } else {
                    String insert = "INSERT INTO TG_Secao (API_Numero, Data_Envio, Data_Aprovacao, Status, Id_Versao) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement psInsert = conn.prepareStatement(insert)) {
                        psInsert.setInt(1, secao.getApiNumero());
                        psInsert.setTimestamp(2, Timestamp.valueOf(secao.getDataEnvio()));
                        if (secao.getDataAprovacao() != null) psInsert.setTimestamp(3, Timestamp.valueOf(secao.getDataAprovacao()));
                        else psInsert.setNull(3, Types.TIMESTAMP);
                        psInsert.setString(4, secao.getStatus() == null ? "Em andamento" : secao.getStatus());
                        psInsert.setInt(5, secao.getIdVersao());
                        psInsert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro no upsert de TG_Secao: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna todas as seções com dados da última versão, para exibir em cards.
     */
    public List<CardDados> listarCards() {
        String sql = "SELECT s.Id_Secao, s.API_Numero, s.Data_Envio, s.Status, v.Semestre_Curso, v.Ano, v.Semestre_Ano " +
                "FROM TG_Secao s " +
                "JOIN TG_Versao v ON v.Id_Versao = s.Id_Versao " +
                "ORDER BY s.API_Numero";
        List<CardDados> list = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CardDados c = new CardDados();
                c.idSecao = rs.getInt("Id_Secao");
                c.apiNumero = rs.getInt("API_Numero");
                c.dataEnvio = rs.getTimestamp("Data_Envio").toLocalDateTime();
                c.status = rs.getString("Status");
                c.semestreCurso = rs.getString("Semestre_Curso");
                c.ano = rs.getInt("Ano");
                c.semestreAno = rs.getString("Semestre_Ano");
                list.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar cards de TG_Secao: " + e.getMessage(), e);
        }
        return list;
    }

    // DTO para dados dos cards
    public static class CardDados {
        public int idSecao;
        public int apiNumero;
        public LocalDateTime dataEnvio;
        public String status;
        public String semestreCurso;
        public int ano;
        public String semestreAno;
    }
}
