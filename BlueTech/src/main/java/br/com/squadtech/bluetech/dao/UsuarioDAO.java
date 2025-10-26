package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.config.ConnectionFactory;
import br.com.squadtech.bluetech.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    /**
     * Cria a tabela usuario caso não exista, com todos os campos atualizados.
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS usuario (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                nome VARCHAR(150) NOT NULL,
                email VARCHAR(190) NOT NULL UNIQUE,
                senha VARCHAR(255) NOT NULL,
                tipo ENUM('ALUNO','ORIENTADOR','COORDENADOR','ADMIN') NOT NULL,
                ativo BOOLEAN NOT NULL DEFAULT TRUE,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_tipo (tipo)
            ) ENGINE=InnoDB;
        """;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela usuario: " + e.getMessage());
        }
    }

    /**
     * Conta o total de usuários cadastrados.
     */
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

    /**
     * Insere um novo usuário no banco, com a senha criptografada.
     */
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

    /**
     * Busca um usuário pelo e-mail.
     */
    public Usuario findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getLong("id"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setSenha(rs.getString("senha")); // Senha hasheada
                    usuario.setTipo(rs.getString("tipo"));
                    usuario.setAtivo(rs.getBoolean("ativo"));
                    usuario.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    usuario.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return usuario;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuario: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca um usuário pelo e-mail e senha digitada (verifica o hash).
     */
    public Usuario findByEmailAndSenha(String email, String senhaDigitada) {
        Usuario usuario = findByEmail(email);
        if (usuario != null && BCrypt.checkpw(senhaDigitada, usuario.getSenha())) {
            return usuario;
        }
        return null;
    }

    /**
     * Valida o login de um usuário (true se email e senha corretos).
     */
    public boolean validateLogin(String email, String senha) {
        Usuario usuario = findByEmail(email);
        return usuario != null && BCrypt.checkpw(senha, usuario.getSenha());
    }
}
