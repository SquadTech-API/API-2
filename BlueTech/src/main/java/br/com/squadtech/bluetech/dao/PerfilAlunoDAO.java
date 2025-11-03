package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.PerfilAluno;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PerfilAlunoDAO {

    private static final Logger log = LoggerFactory.getLogger(PerfilAlunoDAO.class);

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
     * Verifica se o perfil do aluno está completo (com campos principais preenchidos).
     * Não exige idade nem foto porque podem ser opcionais.
     */
    public boolean isPerfilCompleto(String emailUsuario) {
        String sql = "SELECT historico_academico, motivacao, historico_profissional, link_github, link_linkedin, conhecimentos_tecnicos " +
                "FROM Perfil_Aluno WHERE email_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false; // não há perfil
                String histAcad = rs.getString("historico_academico");
                String motiv = rs.getString("motivacao");
                String histProf = rs.getString("historico_profissional");
                String gh = rs.getString("link_github");
                String li = rs.getString("link_linkedin");
                String tech = rs.getString("conhecimentos_tecnicos");
                return notBlank(histAcad) && notBlank(motiv) && notBlank(histProf)
                        && notBlank(gh) && notBlank(li) && notBlank(tech);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar perfil completo: " + e.getMessage(), e);
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
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
            log.error("Erro ao inserir PerfilAluno", e);
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
            log.error("Erro ao atualizar PerfilAluno", e);
            return false;
        }
    }

    // Lista alunos para cards (join com usuario p/ obter nome)
    public List<PerfilAluno> listarAlunosParaCard(String termoNomeOpcional) {
        String base = "SELECT p.id_perfil_aluno, p.email_usuario, p.idade, p.foto, u.nome AS nome_aluno " +
                "FROM Perfil_Aluno p JOIN usuario u ON u.email = p.email_usuario";
        String where = (termoNomeOpcional != null && !termoNomeOpcional.isBlank()) ? " WHERE u.nome LIKE ?" : "";
        String sql = base + where + " ORDER BY u.nome";
        List<PerfilAluno> list = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!where.isEmpty()) {
                ps.setString(1, "%" + termoNomeOpcional + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PerfilAluno a = new PerfilAluno();
                    a.setIdPerfilAluno(rs.getInt("id_perfil_aluno"));
                    a.setEmailUsuario(rs.getString("email_usuario"));
                    int idade = rs.getInt("idade");
                    a.setIdade(rs.wasNull() ? null : idade);
                    a.setFoto(rs.getString("foto"));
                    a.setNomeAluno(rs.getString("nome_aluno"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar alunos: " + e.getMessage(), e);
        }
        return list;
    }

    public String getEmailByPerfilId(int idPerfilAluno) {
        String sql = "SELECT email_usuario FROM Perfil_Aluno WHERE id_perfil_aluno = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPerfilAluno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar email por id_perfil_aluno: " + e.getMessage(), e);
        }
        return null;
    }
}