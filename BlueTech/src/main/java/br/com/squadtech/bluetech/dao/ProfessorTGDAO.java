package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.model.ProfessorTG;
import br.com.squadtech.bluetech.config.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessorTGDAO {

    private static final Logger log = LoggerFactory.getLogger(ProfessorTGDAO.class);

    public ProfessorTGDAO() {
        ensureFotoColumnExists();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

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
                foto VARCHAR(255),
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
                "INSERT INTO professor_tg (nome, usuario_email, cargo, tipo_tg, curso_vinculado, formacao_academica, areas_especializacao, foto) VALUES (?, ?, ?, ?, ?, ?, ?, ?)" :
                "UPDATE professor_tg SET nome = ?, usuario_email = ?, cargo = ?, tipo_tg = ?, curso_vinculado = ?, formacao_academica = ?, areas_especializacao = ?, foto = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, professor.getNome());
            stmt.setString(2, normalizeEmail(professor.getEmail()));
            stmt.setString(3, professor.getCargo());
            stmt.setString(4, professor.getTipoTG());
            stmt.setString(5, professor.getCursoVinculado());
            stmt.setString(6, professor.getFormacaoAcademica());
            stmt.setString(7, professor.getAreasEspecializacao());
            stmt.setString(8, professor.getFoto());

            if (professor.getId() != 0) {
                stmt.setInt(9, professor.getId());
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

    public ProfessorTG findByUsuarioEmail(String email) {
        String sql = "SELECT * FROM professor_tg WHERE LOWER(usuario_email) = LOWER(?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, normalizeEmail(email));
            try (ResultSet rs = stmt.executeQuery()) {
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
                    professor.setFoto(rs.getString("foto"));
                    return professor;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar professor TG pelo email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
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
                professor.setFoto(rs.getString("foto"));

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
                professor.setFoto(rs.getString("foto"));
                return professor;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar professor TG: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean atualizarPerfil(ProfessorTG professor) {
        String sql = "UPDATE professor_tg SET nome = ?, cargo = ?, curso_vinculado = ?, formacao_academica = ?, areas_especializacao = ?, foto = ? WHERE LOWER(usuario_email) = LOWER(?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, professor.getNome());
            stmt.setString(2, professor.getCargo());
            stmt.setString(3, professor.getCursoVinculado());
            stmt.setString(4, professor.getFormacaoAcademica());
            stmt.setString(5, professor.getAreasEspecializacao());
            stmt.setString(6, professor.getFoto());
            stmt.setString(7, normalizeEmail(professor.getEmail()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar perfil do professor TG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean existePorEmail(String email) {
        String sql = "SELECT COUNT(*) FROM professor_tg WHERE LOWER(usuario_email) = LOWER(?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, normalizeEmail(email));
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

    private void ensureFotoColumnExists() {
        String tableName = "professor_tg";
        String columnName = "foto";
        try (Connection conn = ConnectionFactory.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet columns = metaData.getColumns(conn.getCatalog(), null, tableName, columnName)) {
                if (columns.next()) {
                    return; // coluna já existe
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " VARCHAR(255)")) {
                stmt.executeUpdate();
                log.info("Coluna '{}' adicionada à tabela '{}'", columnName, tableName);
            }
        } catch (SQLException e) {
            log.warn("Não foi possível garantir a coluna '{}' na tabela '{}': {}", columnName, tableName, e.getMessage());
        }
    }
}