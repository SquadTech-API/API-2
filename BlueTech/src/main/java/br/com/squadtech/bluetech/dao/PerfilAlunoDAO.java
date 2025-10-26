package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.PerfilAluno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerfilAlunoDAO {

    public PerfilAlunoDAO() {}

    // Criação da tabela
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS perfil_aluno (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                usuario_email VARCHAR(190) NOT NULL,
                idade INT NULL,
                foto VARCHAR(255) NULL,
                historico_academico TEXT NULL,
                historico_profissional TEXT NULL,
                motivacao TEXT NULL,
                link_github VARCHAR(255) NULL,
                link_linkedin VARCHAR(255) NULL,
                conhecimentos_tecnicos TEXT NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT uk_perfil_aluno_usuario UNIQUE (usuario_email),
                CONSTRAINT fk_perfil_aluno_usuario FOREIGN KEY (usuario_email)
                    REFERENCES usuario(email)
                    ON DELETE RESTRICT
                    ON UPDATE CASCADE
            ) ENGINE=InnoDB;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela perfil_aluno: " + e.getMessage(), e);
        }
    }

    // Verifica se existe perfil
    public boolean existePerfil(String emailUsuario) {
        String sql = "SELECT 1 FROM perfil_aluno WHERE usuario_email = ?";
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

    // Busca perfil pelo email
    public PerfilAluno getPerfilByEmail(String emailUsuario) {
        String sql = "SELECT * FROM perfil_aluno WHERE usuario_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerfilAluno(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar perfil: " + e.getMessage(), e);
        }
        return null;
    }

    // Insere novo perfil
    public boolean inserirPerfil(PerfilAluno perfil) {
        String sql = """
            INSERT INTO perfil_aluno
            (usuario_email, idade, foto, historico_academico, motivacao, historico_profissional,
             link_github, link_linkedin, conhecimentos_tecnicos)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setPreparedStatementFromPerfil(stmt, perfil);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir PerfilAluno: " + e.getMessage());
            return false;
        }
    }

    // Atualiza perfil existente
    public boolean atualizarPerfil(PerfilAluno perfil) {
        String sql = """
            UPDATE perfil_aluno SET
                idade = ?, foto = ?, historico_academico = ?, motivacao = ?, 
                historico_profissional = ?, link_github = ?, link_linkedin = ?, conhecimentos_tecnicos = ?
            WHERE usuario_email = ?
        """;

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

    // Lista todos os perfis de alunos com o nome do usuário (JOIN)
    public List<PerfilAluno> listarAlunosComNome() {
        List<PerfilAluno> alunos = new ArrayList<>();
        String sql = """
            SELECT pa.*, u.nome AS nome_aluno
            FROM perfil_aluno pa
            JOIN usuario u ON pa.usuario_email = u.email
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PerfilAluno aluno = mapResultSetToPerfilAluno(rs);
                aluno.setNomeAluno(rs.getString("nome_aluno"));
                alunos.add(aluno);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return alunos;
    }

    // ---------- MÉTODOS AUXILIARES ----------

    private PerfilAluno mapResultSetToPerfilAluno(ResultSet rs) throws SQLException {
        PerfilAluno perfil = new PerfilAluno();
        perfil.setIdPerfilAluno(rs.getInt("id"));

        perfil.setEmailUsuario(rs.getString("usuario_email"));

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

    private void setPreparedStatementFromPerfil(PreparedStatement stmt, PerfilAluno perfil) throws SQLException {
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
    }
}
