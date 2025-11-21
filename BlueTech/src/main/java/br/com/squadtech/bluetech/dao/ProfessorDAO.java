package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Professor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfessorDAO {

    /**
     * Cria a tabela professor caso não exista
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS professor (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                usuario_email VARCHAR(190) NOT NULL,
                cargo VARCHAR(150),
                tipo_tg ENUM('TG1','TG2','AMBOS','NENHUM') NOT NULL DEFAULT 'NENHUM',
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                curso_vinculado VARCHAR(100),
                formacao_academica TEXT,
                areas_especializacao TEXT,
                CONSTRAINT uk_professor_usuario UNIQUE (usuario_email),
                CONSTRAINT fk_professor_usuario FOREIGN KEY (usuario_email)
                    REFERENCES usuario(email) ON DELETE RESTRICT ON UPDATE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela professor: " + e.getMessage(), e);
        }
    }

    /**
     * Insere um novo professor
     */
    public boolean inserirProfessor(Professor professor) {
        String sql = "INSERT INTO professor (usuario_email, cargo, tipo_tg) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, professor.getUsuarioEmail());
            stmt.setString(2, professor.getCargo());
            stmt.setString(3, professor.getTipoTG());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir professor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atualiza um professor existente pelo email do usuário
     */
    public boolean atualizarProfessor(Professor professor) {
        String sql = "UPDATE professor SET cargo = ?, tipo_tg = ? WHERE usuario_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, professor.getCargo());
            stmt.setString(2, professor.getTipoTG());
            stmt.setString(3, professor.getUsuarioEmail());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar professor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca um professor pelo email do usuário
     */
    public Professor findByUsuarioEmail(String email) {
        String sql = "SELECT * FROM professor WHERE usuario_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Professor professor = new Professor();
                    professor.setId(rs.getLong("id"));
                    professor.setUsuarioEmail(rs.getString("usuario_email"));
                    professor.setCargo(rs.getString("cargo"));
                    professor.setTipoTG(rs.getString("tipo_tg"));
                    professor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    professor.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return professor;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar professor: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Lista todos os professores
     */
    public List<Professor> listarProfessores() {
        List<Professor> professores = new ArrayList<>();
        String sql = "SELECT * FROM professor";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Professor professor = new Professor();
                professor.setId(rs.getLong("id"));
                professor.setUsuarioEmail(rs.getString("usuario_email"));
                professor.setCargo(rs.getString("cargo"));
                professor.setTipoTG(rs.getString("tipo_tg"));
                professor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                professor.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                professores.add(professor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return professores;
    }

    /** Busca professor por id (PK) */
    public Professor findById(Long id) {
        String sql = "SELECT * FROM professor WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Professor professor = new Professor();
                    professor.setId(rs.getLong("id"));
                    professor.setUsuarioEmail(rs.getString("usuario_email"));
                    professor.setCargo(rs.getString("cargo"));
                    professor.setTipoTG(rs.getString("tipo_tg"));
                    professor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    professor.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return professor;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar professor por id: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Salva um novo professor orientador (com todas as colunas novas)
     * Inclui criação do usuário + professor em transação
     */
    public boolean salvarProfessorOrientador(String email, String nome, String senhaHash, String tipo,
                                             String cargo, String cursoVinculado, String formacaoAcademica,
                                             String areasEspecializacao) {
        Connection conn = null;
        PreparedStatement stmtUsuario = null;
        PreparedStatement stmtProfessor = null;

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Inserir na tabela usuario
            String sqlUsuario = "INSERT INTO usuario (email, nome, senha, tipo) VALUES (?, ?, ?, ?)";
            stmtUsuario = conn.prepareStatement(sqlUsuario);
            stmtUsuario.setString(1, email);
            stmtUsuario.setString(2, nome);
            stmtUsuario.setString(3, senhaHash);
            stmtUsuario.setString(4, tipo);
            stmtUsuario.executeUpdate();

            // 2. Inserir na tabela professor (com todas as colunas novas)
            String sqlProfessor = "INSERT INTO professor (usuario_email, cargo, curso_vinculado, formacao_academica, areas_especializacao, tipo_tg) VALUES (?, ?, ?, ?, ?, 'NENHUM')";
            stmtProfessor = conn.prepareStatement(sqlProfessor);
            stmtProfessor.setString(1, email);
            stmtProfessor.setString(2, cargo);
            stmtProfessor.setString(3, cursoVinculado);
            stmtProfessor.setString(4, formacaoAcademica);
            stmtProfessor.setString(5, areasEspecializacao);
            stmtProfessor.executeUpdate();

            conn.commit(); // Confirma transação
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback em caso de erro
            } catch (SQLException ex) {
                System.err.println("Erro no rollback: " + ex.getMessage());
            }
            System.err.println("Erro ao salvar professor orientador: " + e.getMessage());
            return false;
        } finally {
            // Fechar recursos
            try {
                if (stmtUsuario != null) stmtUsuario.close();
                if (stmtProfessor != null) stmtProfessor.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Busca todos os professores orientadores cadastrados (com JOIN com usuario)
     */
    public List<ProfessorOrientadorDTO> listarProfessoresOrientadores() {
        List<ProfessorOrientadorDTO> professores = new ArrayList<>();
        String sql = "SELECT u.email, u.nome, p.cargo, p.curso_vinculado, p.formacao_academica, p.areas_especializacao " +
                "FROM usuario u " +
                "INNER JOIN professor p ON u.email = p.usuario_email " +
                "WHERE u.tipo = 'ORIENTADOR'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ProfessorOrientadorDTO professor = new ProfessorOrientadorDTO(
                        rs.getString("email"),
                        rs.getString("nome"),
                        rs.getString("cargo"),
                        rs.getString("curso_vinculado"),
                        rs.getString("formacao_academica"),
                        rs.getString("areas_especializacao")
                );
                professores.add(professor);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar professores orientadores: " + e.getMessage());
        }
        return professores;
    }

    /**
     * Verifica se um email já está cadastrado
     */
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar email: " + e.getMessage());
        }
        return false;
    }

    /**
     * Exclui um professor pelo email (e também o usuário associado)
     */
    public boolean excluirProfessor(String email) {
        Connection conn = null;
        PreparedStatement stmtProfessor = null;
        PreparedStatement stmtUsuario = null;

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Primeiro exclui o professor (devido à foreign key)
            String sqlProfessor = "DELETE FROM professor WHERE usuario_email = ?";
            stmtProfessor = conn.prepareStatement(sqlProfessor);
            stmtProfessor.setString(1, email);
            stmtProfessor.executeUpdate();

            // 2. Depois exclui o usuário
            String sqlUsuario = "DELETE FROM usuario WHERE email = ?";
            stmtUsuario = conn.prepareStatement(sqlUsuario);
            stmtUsuario.setString(1, email);
            int linhasAfetadas = stmtUsuario.executeUpdate();

            conn.commit(); // Confirma transação
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback em caso de erro
            } catch (SQLException ex) {
                System.err.println("Erro no rollback: " + ex.getMessage());
            }
            System.err.println("Erro ao excluir professor: " + e.getMessage());
            return false;
        } finally {
            // Fechar recursos
            try {
                if (stmtProfessor != null) stmtProfessor.close();
                if (stmtUsuario != null) stmtUsuario.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    /**
     * DTO específico para professores orientadores
     */
    public static class ProfessorOrientadorDTO {
        private String email;
        private String nome;
        private String cargo;
        private String cursoVinculado;
        private String formacaoAcademica;
        private String areasEspecializacao;

        public ProfessorOrientadorDTO(String email, String nome, String cargo, String cursoVinculado,
                                      String formacaoAcademica, String areasEspecializacao) {
            this.email = email;
            this.nome = nome;
            this.cargo = cargo;
            this.cursoVinculado = cursoVinculado;
            this.formacaoAcademica = formacaoAcademica;
            this.areasEspecializacao = areasEspecializacao;
        }

        // Getters
        public String getEmail() { return email; }
        public String getNome() { return nome; }
        public String getCargo() { return cargo; }
        public String getCursoVinculado() { return cursoVinculado; }
        public String getFormacaoAcademica() { return formacaoAcademica; }
        public String getAreasEspecializacao() { return areasEspecializacao; }
    }
}