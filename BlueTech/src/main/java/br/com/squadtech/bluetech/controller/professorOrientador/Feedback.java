package br.com.squadtech.bluetech.controller.professorOrientador;

public class Feedback {

    private int id;
    private int versaoId;
    private int professorId;
    private String status; // Deve ser "ACEITO" ou "AJUSTE"
    private String comentario;
    // Não precisamos de created_at/updated_at, o banco de dados cuida disso.

    /**
     * Construtor usado para SALVAR um novo feedback.
     * O ID é gerado pelo banco, por isso não está aqui.
     */
    public Feedback(int versaoId, int professorId, String status, String comentario) {
        this.versaoId = versaoId;
        this.professorId = professorId;
        this.status = status;
        this.comentario = comentario;
    }

    // --- Getters e Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersaoId() {
        return versaoId;
    }

    public void setVersaoId(int versaoId) {
        this.versaoId = versaoId;
    }

    public int getProfessorId() {
        return professorId;
    }

    public void setProfessorId(int professorId) {
        this.professorId = professorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}