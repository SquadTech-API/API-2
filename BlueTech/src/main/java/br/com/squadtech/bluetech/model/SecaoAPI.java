package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

public class SecaoAPI {
    // Identificador
    private Integer idSecaoApi;

    // Campos do formulário
    private String semestre;
    private Integer ano;
    private String semestreAno;
    private String empresa;
    private String problema;
    private String solucao;
    private String repositorio;
    private String linkedin;
    private String tecnologias;
    private String contribuicoes;
    private String hardSkills;
    private String softSkills;

    // Auditoria
    private LocalDateTime createdAt;

    public SecaoAPI() {}

    public SecaoAPI(String semestre, Integer ano, String semestreAno, String empresa,
                    String problema, String solucao, String repositorio, String linkedin, String tecnologias,
                    String contribuicoes, String hardSkills, String softSkills) {
        this.semestre = semestre;
        this.ano = ano;
        this.semestreAno = semestreAno;
        this.empresa = empresa;
        this.problema = problema;
        this.solucao = solucao;
        this.repositorio = repositorio;
        this.linkedin = linkedin;
        this.tecnologias = tecnologias;
        this.contribuicoes = contribuicoes;
        this.hardSkills = hardSkills;
        this.softSkills = softSkills;
    }

    public Integer getIdSecaoApi() { return idSecaoApi; }
    public void setIdSecaoApi(Integer idSecaoApi) { this.idSecaoApi = idSecaoApi; }

    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public String getSemestreAno() { return semestreAno; }
    public void setSemestreAno(String semestreAno) { this.semestreAno = semestreAno; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getProblema() { return problema; }
    public void setProblema(String problema) { this.problema = problema; }

    public String getSolucao() { return solucao; }
    public void setSolucao(String solucao) { this.solucao = solucao; }

    public String getRepositorio() { return repositorio; }
    public void setRepositorio(String repositorio) { this.repositorio = repositorio; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getTecnologias() { return tecnologias; }
    public void setTecnologias(String tecnologias) { this.tecnologias = tecnologias; }

    public String getContribuicoes() { return contribuicoes; }
    public void setContribuicoes(String contribuicoes) { this.contribuicoes = contribuicoes; }

    public String getHardSkills() { return hardSkills; }
    public void setHardSkills(String hardSkills) { this.hardSkills = hardSkills; }

    public String getSoftSkills() { return softSkills; }
    public void setSoftSkills(String softSkills) { this.softSkills = softSkills; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
