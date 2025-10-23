package br.com.squadtech.bluetech.model;

public class PerfilAluno {
    private Integer idPerfilAluno;
    private String emailUsuario; //FK para usuario.email
    private Integer idade; //pode ser nulo
    private String foto; //caminho absoluto para o arquivo
    private String historicoAcademico;
    private String motivacao;
    private String historicoProfissional;
    private String linkGithub;
    private String linkLinkedin;
    private String conhecimentosTecnicos;

    //Construtores, getters e setters
    public PerfilAluno() {}

    public PerfilAluno(Integer idPerfilAluno, String emailUsuario, Integer idade, String foto,
                       String historicoAcademico, String motivacao, String historicoProfissional,
                       String linkGithub, String linkLinkedin, String conhecimentosTecnicos) {
        this.idPerfilAluno = idPerfilAluno;
        this.emailUsuario = emailUsuario;
        this.idade = idade;
        this.foto = foto;
        this.historicoAcademico = historicoAcademico;
        this.motivacao = motivacao;
        this.historicoProfissional = historicoProfissional;
        this.linkGithub = linkGithub;
        this.linkLinkedin = linkLinkedin;
        this.conhecimentosTecnicos = conhecimentosTecnicos;
    }


    public Integer getIdPerfilAluno() { return idPerfilAluno; }
    public void setIdPerfilAluno(Integer idPerfilAluno) { this.idPerfilAluno = idPerfilAluno; }

    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }

    public Integer getIdade() { return idade; }
    public void setIdade(Integer idade) { this.idade = idade; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getHistoricoAcademico() { return historicoAcademico; }
    public void setHistoricoAcademico(String historicoAcademico) { this.historicoAcademico = historicoAcademico; }

    public String getMotivacao() { return motivacao; }
    public void setMotivacao(String motivacao) { this.motivacao = motivacao; }

    public String getHistoricoProfissional() { return historicoProfissional; }
    public void setHistoricoProfissional(String historicoProfissional) { this.historicoProfissional = historicoProfissional; }

    public String getLinkGithub() { return linkGithub; }
    public void setLinkGithub(String linkGithub) { this.linkGithub = linkGithub; }

    public String getLinkLinkedin() { return linkLinkedin; }
    public void setLinkLinkedin(String linkLinkedin) { this.linkLinkedin = linkLinkedin; }

    public String getConhecimentosTecnicos() { return conhecimentosTecnicos; }
    public void setConhecimentosTecnicos(String conhecimentosTecnicos) { this.conhecimentosTecnicos = conhecimentosTecnicos; }
}
