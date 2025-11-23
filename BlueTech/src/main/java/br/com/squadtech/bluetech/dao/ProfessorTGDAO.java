package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.model.ProfessorTG;
import br.com.squadtech.bluetech.config.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessorTGDAO {

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS professor_tg (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nome VARCHAR(150) NOT NULL,
                usuario_email VARCHAR(190) NOT NULL,
                cargo VARCHAR(150),
                tipo_tg ENUM('TG1','TG2','AMBOS','NENHUM') NOT NULL DEFAULT 'NENHUM',
                curso_vinculado VARCHAR(150),
                formacao_academica TEXT,
                areas_especializacao TEXT,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT uk_professor_tg_email UNIQUE (usuario_email),
                CONSTRAINT fk_professor_tg_usuario FOREIGN KEY (usuario_email)
                    REFERENCES usuario(email) ON DELETE RESTRICT ON UPDATE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela professor_tg: " + e.getMessage(), e);
        }
    }

    public boolean salvar(ProfessorTG professor) {
        String sql = professor.getId() == 0 ?
                "INSERT INTO professor_tg (nome, usuario_email, cargo, tipo_tg, curso_vinculado, formacao_academica, areas_especializacao) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                "UPDATE professor_tg SET nome = ?, usuario_email = ?, cargo = ?, tipo_tg = ?, curso_vinculado = ?, formacao_academica = ?, areas_especializacao = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, professor.getNome());
            stmt.setString(2, professor.getEmail());
            stmt.setString(3, professor.getCargo());
            stmt.setString(4, professor.getTipoTG());
            stmt.setString(5, professor.getCursoVinculado());
            stmt.setString(6, professor.getFormacaoAcademica());
            stmt.setString(7, professor.getAreasEspecializacao());

            if (professor.getId() != 0) {
                stmt.setInt(8, professor.getId());
            }

            int affectedRows = stmt.executeUpdate();

            if (professor.getId() == 0 && affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        professor.setId(rs.getInt(1));
                    }
                }
            }

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao salvar professor TG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<ProfessorTG> listarTodos() {
        List<ProfessorTG> professores = new ArrayList<>();
        String sql = "SELECT * FROM professor_tg ORDER BY nome";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ProfessorTG professor = new ProfessorTG();
                professor.setId(rs.getInt("id"));
                professor.setNome(rs.getString("nome"));
                professor.setEmail(rs.getString("usuario_email"));
                professor.setCargo(rs.getString("cargo"));
                professor.setTipoTG(rs.getString("tipo_tg"));
                professor.setCursoVinculado(rs.getString("curso_vinculado"));
                professor.setFormacaoAcademica(rs.getString("formacao_academica"));
                professor.setAreasEspecializacao(rs.getString("areas_especializacao"));

                professores.add(professor);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar professores TG: " + e.getMessage());
            e.printStackTrace();
        }

        return professores;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM professor_tg WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir professor TG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public ProfessorTG buscarPorId(int id) {
        String sql = "SELECT * FROM professor_tg WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ProfessorTG professor = new ProfessorTG();
                professor.setId(rs.getInt("id"));
                professor.setNome(rs.getString("nome"));
                professor.setEmail(rs.getString("usuario_email"));
                professor.setCargo(rs.getString("cargo"));
                professor.setTipoTG(rs.getString("tipo_tg"));
                professor.setCursoVinculado(rs.getString("curso_vinculado"));
                professor.setFormacaoAcademica(rs.getString("formacao_academica"));
                professor.setAreasEspecializacao(rs.getString("areas_especializacao"));
                return professor;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar professor TG: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean existePorEmail(String email) {
        String sql = "SELECT COUNT(*) FROM professor_tg WHERE usuario_email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao verificar email do professor TG: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public List<ProfessorTG> buscarPorTipoTG(String tipoTG) {
        List<ProfessorTG> professores = new ArrayList<>();
        String sql = "SELECT * FROM professor_tg WHERE tipo_tg = ? ORDER BY nome";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipoTG);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ProfessorTG professor = new ProfessorTG();
                professor.setId(rs.getInt("id"));
                professor.setNome(rs.getString("nome"));
                professor.setEmail(rs.getString("usuario_email"));
                professor.setCargo(rs.getString("cargo"));
                professor.setTipoTG(rs.getString("tipo_tg"));
                professor.setCursoVinculado(rs.getString("curso_vinculado"));
                professor.setFormacaoAcademica(rs.getString("formacao_academica"));
                professor.setAreasEspecializacao(rs.getString("areas_especializacao"));

                professores.add(professor);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar professores TG por tipo: " + e.getMessage());
            e.printStackTrace();
        }

        return professores;
    }
}