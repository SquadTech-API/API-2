package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

/**
 * Modelo de negociação de Agendamento de Defesa (TG).
 * Controla o fluxo aluno-professor até a confirmação.
 */
public class AgendamentoDefesa {
    private Long id;
    private String alunoEmail;    // FK usuario.email
    private Long professorId;     // FK professor.id (orientador ou professor TG)

    private String mensagemAluno;     // última mensagem do aluno (solicitação/recusa)
    private String mensagemProfessor; // última mensagem do professor (proposta/resposta)

    // Proposta atual do professor
    private LocalDateTime propostaDataHora; // data + hora sugerida
    private String sala;                    // sala sugerida (texto livre ou número)

    // Status do fluxo
    // AGUARDANDO_ORIENTADOR -> aguarda professor enviar proposta
    // AGUARDANDO_ALUNO      -> aguarda aluno confirmar/recusar
    // REAGENDAMENTO         -> professor propôs nova data após AGENDADO
    // AGENDADO              -> confirmado por ambos
    private String status;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private LocalDateTime dataConfirmacao; // quando ficou AGENDADO

    public AgendamentoDefesa() {}

    public AgendamentoDefesa(String alunoEmail, Long professorId, String mensagemAluno) {
        this.alunoEmail = alunoEmail;
        this.professorId = professorId;
        this.mensagemAluno = mensagemAluno;
        this.status = "AGUARDANDO_ORIENTADOR";
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = this.dataCriacao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAlunoEmail() { return alunoEmail; }
    public void setAlunoEmail(String alunoEmail) { this.alunoEmail = alunoEmail; }

    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }

    public String getMensagemAluno() { return mensagemAluno; }
    public void setMensagemAluno(String mensagemAluno) { this.mensagemAluno = mensagemAluno; }

    public String getMensagemProfessor() { return mensagemProfessor; }
    public void setMensagemProfessor(String mensagemProfessor) { this.mensagemProfessor = mensagemProfessor; }

    public LocalDateTime getPropostaDataHora() { return propostaDataHora; }
    public void setPropostaDataHora(LocalDateTime propostaDataHora) { this.propostaDataHora = propostaDataHora; }

    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public LocalDateTime getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(LocalDateTime dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }
}

