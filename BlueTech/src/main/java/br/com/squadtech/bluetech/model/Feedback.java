package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;
import java.util.List;
import br.com.squadtech.bluetech.model.FeedbackItem;

public class Feedback {

    // Identificador
    private Integer id;

    // Relações
    private Integer versaoId;       // FK para TGVersao
    private Integer professorId;    // FK para Professor

    // Dados do feedback
    private String status;          // APROVADO, AJUSTES
    private String comentario;      // Comentário geral adicional
    private List<FeedbackItem> itens;

    // Auditoria
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Feedback() {}

    public Feedback(Integer versaoId, Integer professorId, String status, String comentario) {
        this.versaoId = versaoId;
        this.professorId = professorId;
        this.status = status;
        this.comentario = comentario;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVersaoId() { return versaoId; }
    public void setVersaoId(Integer versaoId) { this.versaoId = versaoId; }

    public Integer getProfessorId() { return professorId; }
    public void setProfessorId(Integer professorId) { this.professorId = professorId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public List<FeedbackItem> getItens() { return itens; }
    public void setItens(List<FeedbackItem> itens) { this.itens = itens; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
