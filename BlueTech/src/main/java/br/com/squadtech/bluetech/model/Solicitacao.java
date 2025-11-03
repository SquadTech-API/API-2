package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

public class Solicitacao {
    private Long id;
    private String alunoEmail; // FK usuario.email
    private Long professorId;  // FK professor.id
    private String mensagem;
    private String status; // AGUARDANDO, ACEITO, RECUSADO
    private LocalDateTime dataEnvio;
    private LocalDateTime dataResposta;

    public Solicitacao() {}

    public Solicitacao(String alunoEmail, Long professorId, String mensagem) {
        this.alunoEmail = alunoEmail;
        this.professorId = professorId;
        this.mensagem = mensagem;
        this.status = "AGUARDANDO";
        this.dataEnvio = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAlunoEmail() { return alunoEmail; }
    public void setAlunoEmail(String alunoEmail) { this.alunoEmail = alunoEmail; }

    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }
}

