package br.com.squadtech.bluetech.model;

public class ProfessorTG {
    private int id;
    private String nome; // NOVO CAMPO
    private String email; // usuario_email na tabela
    private String cargo;
    private String tipoTG; // tipo_tg na tabela
    private String cursoVinculado;
    private String formacaoAcademica;
    private String areasEspecializacao;
    private String foto;

    // Construtores
    public ProfessorTG() {}

    public ProfessorTG(String nome, String email, String cargo, String tipoTG, String cursoVinculado) {
        this.nome = nome;
        this.email = email;
        this.cargo = cargo;
        this.tipoTG = tipoTG;
        this.cursoVinculado = cursoVinculado;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getTipoTG() { return tipoTG; }
    public void setTipoTG(String tipoTG) { this.tipoTG = tipoTG; }

    public String getCursoVinculado() { return cursoVinculado; }
    public void setCursoVinculado(String cursoVinculado) { this.cursoVinculado = cursoVinculado; }

    public String getFormacaoAcademica() { return formacaoAcademica; }
    public void setFormacaoAcademica(String formacaoAcademica) { this.formacaoAcademica = formacaoAcademica; }

    public String getAreasEspecializacao() { return areasEspecializacao; }
    public void setAreasEspecializacao(String areasEspecializacao) { this.areasEspecializacao = areasEspecializacao; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    @Override
    public String toString() {
        return nome + " (" + email + ") - " + tipoTG;
    }
}