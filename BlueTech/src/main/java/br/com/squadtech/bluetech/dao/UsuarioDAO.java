package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.model.Usuario;
import br.com.squadtech.bluetech.config.ConnectionFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // =====================================================
    // CREATE TABLE IF NOT EXISTS
    // =====================================================
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS usuario (
                email VARCHAR(190) PRIMARY KEY,
                nome VARCHAR(150) NOT NULL,
                senha_hash VARCHAR(255) NOT NULL,
                tipo ENUM('ALUNO', 'ORIENTADOR', 'PROFESSOR_TG', 'ADMIN') NOT NULL,
                ativo BOOLEAN NOT NULL DEFAULT TRUE,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_tipo (tipo)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela usuario: " + e.getMessage(), e);
        }
    }

    // =====================================================
    // COUNT USUARIOS
    // =====================================================
    public long countUsuarios() {
        String sql = "SELECT COUNT(*) AS total FROM usuario";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // =====================================================
    // INSERT
    // =====================================================
    public void insert(Usuario usuario) {
        String sql = "INSERT INTO usuario (email, nome, senha_hash, tipo) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getEmail());
            stmt.setString(2, usuario.getNome());
            stmt.setString(3, BCrypt.hashpw(usuario.getSenhaHash(), BCrypt.gensalt()));
            stmt.setString(4, usuario.getTipo());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // UPDATE
    // =====================================================
    public void update(Usuario usuario) {
        String sql = "UPDATE usuario SET nome = ?, senha_hash = ?, tipo = ? WHERE email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, BCrypt.hashpw(usuario.getSenhaHash(), BCrypt.gensalt()));
            stmt.setString(3, usuario.getTipo());
            stmt.setString(4, usuario.getEmail());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // DELETE
    // =====================================================
    public void delete(String email) {
        String sql = "DELETE FROM usuario WHERE email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // FIND BY EMAIL (PK)
    // =====================================================
    public Usuario findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        Usuario usuario = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                usuario = new Usuario();
                usuario.setEmail(rs.getString("email"));
                usuario.setNome(rs.getString("nome"));
                usuario.setSenhaHash(rs.getString("senha_hash"));
                usuario.setTipo(rs.getString("tipo"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usuario;
    }

    // =====================================================
    // FIND BY EMAIL AND SENHA
    // =====================================================
    public Usuario findByEmailAndSenha(String email, String senhaDigitada) {
        Usuario usuario = findByEmail(email);

        if (usuario != null && usuario.getSenhaHash() != null) {
            if (BCrypt.checkpw(senhaDigitada, usuario.getSenhaHash())) {
                return usuario;
            }
        }

        return null;
    }

    // =====================================================
    // LISTAR TODOS
    // =====================================================
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setEmail(rs.getString("email"));
                usuario.setNome(rs.getString("nome"));
                usuario.setSenhaHash(rs.getString("senha_hash"));
                usuario.setTipo(rs.getString("tipo"));

                usuarios.add(usuario);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usuarios;
    }
}
