package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

/**
 * Representa uma mensagem trocada entre um aluno e seu orientador.
 */
public class Mensagens {

    private LocalDateTime dataHora; // PK
    private String conteudo;
    private Long professorId;
    private Integer alunoId;
    private Long orientacaoId;
    private Integer secaoId;
    private Boolean enviadoPorProfessor;

    public Mensagens() {}

    public Mensagens(LocalDateTime dataHora, String conteudo, Long professorId, Integer alunoId, Long orientacaoId, Boolean enviadoPorProfessor) {
        this.dataHora = dataHora;
        this.conteudo = conteudo;
        this.professorId = professorId;
        this.alunoId = alunoId;
        this.orientacaoId = orientacaoId;
        this.enviadoPorProfessor = enviadoPorProfessor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public Integer getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Integer alunoId) {
        this.alunoId = alunoId;
    }

    public Long getOrientacaoId() {
        return orientacaoId;
    }

    public void setOrientacaoId(Long orientacaoId) {
        this.orientacaoId = orientacaoId;
    }

    public Integer getSecaoId() {
        return secaoId;
    }

    public void setSecaoId(Integer secaoId) {
        this.secaoId = secaoId;
    }

    public Boolean getEnviadoPorProfessor() {
        return enviadoPorProfessor;
    }

    public void setEnviadoPorProfessor(Boolean enviadoPorProfessor) {
        this.enviadoPorProfessor = enviadoPorProfessor;
    }
}
