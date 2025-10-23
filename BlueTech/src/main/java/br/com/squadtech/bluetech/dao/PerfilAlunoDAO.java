package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.model.PerfilAluno;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PerfilAlunoDAO {

    //Construtor vazio (seguindo padrão do UsuarioDAO que abre conexão dentro de cada método)
    public PerfilAlunoDAO() {
    }

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS Perfil_Aluno (" +
                "id_perfil_aluno INT AUTO_INCREMENT PRIMARY KEY," +
                "email_usuario VARCHAR(255) NOT NULL," +
                "idade INTEGER," +
                "foto VARCHAR(255)," +
                "historico_academico TEXT," +
                "motivacao TEXT," +
                "historico_profissional TEXT," +
                "link_github VARCHAR(255)," +
                "link_linkedin VARCHAR(255)," +
                "conhecimentos_tecnicos TEXT," +
                "FOREIGN KEY (email_usuario) REFERENCES usuario(email)" +
                ")";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela Perfil_Aluno: " + e.getMessage());
        }
    }

    /**
     * Verifica se existe perfil para o email informado.
     */
    public boolean existePerfil(String emailUsuario) {
        String sql = "SELECT 1 FROM Perfil_Aluno WHERE email_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existência de perfil: " + e.getMessage(), e);
        }
    }

    /**
     * Busca o perfil pelo email_usuario.
     */
    public PerfilAluno getPerfilByEmail(String emailUsuario) {
        String sql = "SELECT * FROM Perfil_Aluno WHERE email_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PerfilAluno perfil = new PerfilAluno();
                    perfil.setIdPerfilAluno(rs.getInt("id_perfil_aluno"));
                    perfil.setEmailUsuario(rs.getString("email_usuario"));

                    int idadeVal = rs.getInt("idade");
                    perfil.setIdade(rs.wasNull() ? null : idadeVal);

                    perfil.setFoto(rs.getString("foto"));
                    perfil.setHistoricoAcademico(rs.getString("historico_academico"));
                    perfil.setMotivacao(rs.getString("motivacao"));
                    perfil.setHistoricoProfissional(rs.getString("historico_profissional"));
                    perfil.setLinkGithub(rs.getString("link_github"));
                    perfil.setLinkLinkedin(rs.getString("link_linkedin"));
                    perfil.setConhecimentosTecnicos(rs.getString("conhecimentos_tecnicos"));
                    return perfil;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar perfil: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Insere um novo perfil.
     */
    public boolean inserirPerfil(PerfilAluno perfil) {
        String sql = "INSERT INTO Perfil_Aluno (email_usuario, idade, foto, historico_academico, motivacao, historico_profissional, link_github, link_linkedin, conhecimentos_tecnicos) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, perfil.getEmailUsuario());

            if (perfil.getIdade() != null) stmt.setInt(2, perfil.getIdade());
            else stmt.setNull(2, Types.INTEGER);

            stmt.setString(3, perfil.getFoto());
            stmt.setString(4, perfil.getHistoricoAcademico());
            stmt.setString(5, perfil.getMotivacao());
            stmt.setString(6, perfil.getHistoricoProfissional());
            stmt.setString(7, perfil.getLinkGithub());
            stmt.setString(8, perfil.getLinkLinkedin());
            stmt.setString(9, perfil.getConhecimentosTecnicos());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir PerfilAluno: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atualiza perfil existente identificado por email_usuario.
     */
    public boolean atualizarPerfil(PerfilAluno perfil) {
        String sql = "UPDATE Perfil_Aluno SET idade = ?, foto = ?, historico_academico = ?, motivacao = ?, historico_profissional = ?, link_github = ?, link_linkedin = ?, conhecimentos_tecnicos = ? WHERE email_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (perfil.getIdade() != null) stmt.setInt(1, perfil.getIdade());
            else stmt.setNull(1, Types.INTEGER);

            stmt.setString(2, perfil.getFoto());
            stmt.setString(3, perfil.getHistoricoAcademico());
            stmt.setString(4, perfil.getMotivacao());
            stmt.setString(5, perfil.getHistoricoProfissional());
            stmt.setString(6, perfil.getLinkGithub());
            stmt.setString(7, perfil.getLinkLinkedin());
            stmt.setString(8, perfil.getConhecimentosTecnicos());

            stmt.setString(9, perfil.getEmailUsuario());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar PerfilAluno: " + e.getMessage());
            return false;
        }
    }
}