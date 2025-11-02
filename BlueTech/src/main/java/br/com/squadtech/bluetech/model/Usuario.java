package br.com.squadtech.bluetech.model;

public class Usuario {
    private String email;
    private String nome;
    private String senha;
    private String tipo; //"ADMIN", "PROF_TG", "ORIENTADOR", "ALUNO"

    //Construtores, getters e setters
    public Usuario() {}

    public Usuario(String email, String nome, String senha, String tipo) {
        this.email = email;
        this.nome = nome;
        this.senha = senha;
        this.tipo = tipo;
    }


    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}