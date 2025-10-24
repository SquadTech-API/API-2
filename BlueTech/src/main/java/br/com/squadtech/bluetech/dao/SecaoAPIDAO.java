package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.SecaoAPI;

import java.sql.*;

public class SecaoAPIDAO {

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS TG_Versao (" +
                "Id_Versao INT AUTO_INCREMENT PRIMARY KEY," +
                "Semestre_Curso VARCHAR(50)," +
                "Ano INT," +
                "Semestre_Ano VARCHAR(50)," +
                "Empresa_Parceira VARCHAR(255)," +
                "Problema TEXT," +
                "Solucao TEXT," +
                "Link_Repositorio VARCHAR(255)," +
                "Link_Linkedin VARCHAR(255)," +
                "Tecnologias TEXT," +
                "Contribuicoes TEXT," +
                "Hard_Skills TEXT," +
                "Soft_Skills TEXT," +
                "Data_Criacao DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela TG_Versao: " + e.getMessage(), e);
        }
    }

    public Integer insertReturningId(SecaoAPI secao) {
        String sql = "INSERT INTO TG_Versao (Semestre_Curso, Ano, Semestre_Ano, Empresa_Parceira, Problema, Solucao, Link_Repositorio, Link_Linkedin, Tecnologias, Contribuicoes, Hard_Skills, Soft_Skills) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, secao.getSemestre());
            if (secao.getAno() != null) stmt.setInt(2, secao.getAno()); else stmt.setNull(2, java.sql.Types.INTEGER);
            stmt.setString(3, secao.getSemestreAno());
            stmt.setString(4, secao.getEmpresa());
            stmt.setString(5, secao.getProblema());
            stmt.setString(6, secao.getSolucao());
            stmt.setString(7, secao.getRepositorio());
            stmt.setString(8, secao.getLinkedin());
            stmt.setString(9, secao.getTecnologias());
            stmt.setString(10, secao.getContribuicoes());
            stmt.setString(11, secao.getHardSkills());
            stmt.setString(12, secao.getSoftSkills());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir TG_Versao: " + e.getMessage());
            return null;
        }
    }

    // Mantém o método antigo para compatibilidade, chamando o novo
    public boolean insert(SecaoAPI secao) {
        return insertReturningId(secao) != null;
    }
}
