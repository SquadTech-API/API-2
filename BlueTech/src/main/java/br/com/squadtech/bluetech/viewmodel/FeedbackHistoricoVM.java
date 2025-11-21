package br.com.squadtech.bluetech.viewmodel;

import java.time.LocalDateTime;

public class FeedbackHistoricoVM {
    private Long id;
    private Long versaoId;
    private Integer versaoNumero;
    private Integer apiNumero;
    private String status;            // APROVADO / AJUSTES / REJEITADO (ou similar)
    private String comentarioGeral;   // Comentário do feedback
    private String professorNome;
    private LocalDateTime criadoEm;

    public FeedbackHistoricoVM() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersaoId() { return versaoId; }
    public void setVersaoId(Long versaoId) { this.versaoId = versaoId; }

    public Integer getVersaoNumero() { return versaoNumero; }
    public void setVersaoNumero(Integer versaoNumero) { this.versaoNumero = versaoNumero; }

    public Integer getApiNumero() { return apiNumero; }
    public void setApiNumero(Integer apiNumero) { this.apiNumero = apiNumero; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComentarioGeral() { return comentarioGeral; }
    public void setComentarioGeral(String comentarioGeral) { this.comentarioGeral = comentarioGeral; }

    public String getProfessorNome() { return professorNome; }
    public void setProfessorNome(String professorNome) { this.professorNome = professorNome; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
