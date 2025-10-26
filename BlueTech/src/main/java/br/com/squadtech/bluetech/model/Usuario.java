package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

public class Usuario {

    private Long id;
    private String email;
    private String nome;
    private String senha;
    private String tipo; // "ADMIN", "COORDENADOR", "ORIENTADOR", "ALUNO"
    private boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtores
    public Usuario() {}

    public Usuario(Long id, String email, String nome, String senha, String tipo, boolean ativo,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.senha = senha;
        this.tipo = tipo;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor simplificado (para cadastro rápido)
    public Usuario(String email, String nome, String senha, String tipo) {
        this.email = email;
        this.nome = nome;
        this.senha = senha;
        this.tipo = tipo;
        this.ativo = true;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Para debug e logs
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", tipo='" + tipo + '\'' +
                ", ativo=" + ativo +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
