package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS usuario (" +
                "email VARCHAR(255) PRIMARY KEY," +
                "nome VARCHAR(100) NOT NULL," +
                "senha VARCHAR(255) NOT NULL," +
                "tipo VARCHAR(50) NOT NULL" +
                ")";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela usuario: " + e.getMessage());
        }
    }

    public long countUsuarios() {
        String sql = "SELECT COUNT(*) FROM usuario";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar usuarios: " + e.getMessage());
        }
        return 0;
    }

    public void insert(Usuario usuario) {
        String sql = "INSERT INTO usuario (email, nome, senha, tipo) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getEmail());
            stmt.setString(2, usuario.getNome());
            stmt.setString(3, BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt())); // Hash da senha
            stmt.setString(4, usuario.getTipo());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir usuario: " + e.getMessage());
        }
    }

    public Usuario findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setEmail(rs.getString("email"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setSenha(rs.getString("senha")); // Senha hasheada
                    usuario.setTipo(rs.getString("tipo"));
                    return usuario;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuario: " + e.getMessage());
        }
        return null;
    }

    public Usuario findByEmailAndSenha(String email, String senhaDigitada) {
    Usuario usuario = findByEmail(email);
        if (usuario != null && org.mindrot.jbcrypt.BCrypt.checkpw(senhaDigitada, usuario.getSenha())) {
            return usuario;
        }
        return null;
    }


    public boolean validateLogin(String email, String senha) {
        Usuario usuario = findByEmail(email);
        return usuario != null && BCrypt.checkpw(senha, usuario.getSenha());
    }
}