package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

public class TGVersao {

    // Identificador da versão
    private Long id;                  // corresponde a id AUTO_INCREMENT
    private Long secaoId;             // FK para tg_secao
    private Integer numeroVersao;     // Número da versão (novo campo)

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

    // Status e auditoria
    private Boolean aceita;
    private Boolean avaliado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🔹 Construtor padrão
    public TGVersao() {}

    // 🔹 Construtor usado no CriarSecaoAPIController (12 parâmetros)
    public TGVersao(String semestre, Integer ano, String semestreAno, String empresa,
                    String problema, String solucao, String repositorio, String linkedin,
                    String tecnologias, String contribuicoes, String hardSkills, String softSkills) {
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
        this.aceita = false;
        this.avaliado = false;
    }

    // 🔹 Construtor completo (mantido)
    public TGVersao(Long secaoId, String semestre, Integer ano, String semestreAno, String empresa,
                    String problema, String solucao, String repositorio, String linkedin, String tecnologias,
                    String contribuicoes, String hardSkills, String softSkills, Boolean aceita, Boolean avaliado) {
        this.secaoId = secaoId;
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
        this.aceita = aceita;
        this.avaliado = avaliado;
    }

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSecaoId() { return secaoId; }
    public void setSecaoId(Long secaoId) { this.secaoId = secaoId; }

    public Integer getNumeroVersao() { return numeroVersao; }
    public void setNumeroVersao(Integer numeroVersao) { this.numeroVersao = numeroVersao; }

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

    public Boolean getAceita() { return aceita; }
    public void setAceita(Boolean aceita) { this.aceita = aceita; }

    public Boolean getAvaliado() { return avaliado; }
    public void setAvaliado(Boolean avaliado) { this.avaliado = avaliado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
