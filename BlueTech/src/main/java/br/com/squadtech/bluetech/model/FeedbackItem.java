package br.com.squadtech.bluetech.model;

public class FeedbackItem {

    private Integer id;
    private Integer feedbackId; // FK para Feedback
    private String campo;      // Nome do campo da TGVersao (ex: "empresa", "problema")
    private String status;     // OK, AJUSTE
    private String comentario; // Comentário específico para o item

    public FeedbackItem() {}

    public FeedbackItem(Integer feedbackId, String campo, String status, String comentario) {
        this.feedbackId = feedbackId;
        this.campo = campo;
        this.status = status;
        this.comentario = comentario;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Integer feedbackId) { this.feedbackId = feedbackId; }

    public String getCampo() { return campo; }
    public void setCampo(String campo) { this.campo = campo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
