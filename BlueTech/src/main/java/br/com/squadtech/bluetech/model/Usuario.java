package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

public class Usuario {

    private String email;            // PK
    private String nome;
    private String senhaHash;        // Senha armazenada como hash
    private String tipo;             // "ALUNO", "ORIENTADOR", "PROFESSOR_TG"
    private boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public Usuario() {}

    // Construtor completo
    public Usuario(String email, String nome, String senhaHash, String tipo,
                   boolean ativo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.email = email;
        this.nome = nome;
        this.senhaHash = senhaHash;
        this.tipo = tipo;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor simplificado para novo usuário (ativo por padrão)
    public Usuario(String email, String nome, String senhaHash, String tipo) {
        this.email = email;
        this.nome = nome;
        this.senhaHash = senhaHash;
        this.tipo = tipo;
        this.ativo = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters e Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Usuario{" +
                "email='" + email + '\'' +
                ", nome='" + nome + '\'' +
                ", tipo='" + tipo + '\'' +
                ", ativo=" + ativo +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }



}
