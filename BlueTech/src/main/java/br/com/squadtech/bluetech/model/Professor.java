package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

public class Professor {

    private Long id;
    private String usuarioEmail;   // FK para usuario.email
    private String cargo;
    private String tipoTG;         // "TG1", "TG2", "AMBOS", "NENHUM"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor vazio
    public Professor() {}

    // Construtor simplificado
    public Professor(String usuarioEmail, String cargo, String tipoTG) {
        this.usuarioEmail = usuarioEmail;
        this.cargo = cargo;
        this.tipoTG = tipoTG;
    }

    // Construtor completo
    public Professor(Long id, String usuarioEmail, String cargo, String tipoTG,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.usuarioEmail = usuarioEmail;
        this.cargo = cargo;
        this.tipoTG = tipoTG;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getTipoTG() { return tipoTG; }
    public void setTipoTG(String tipoTG) { this.tipoTG = tipoTG; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Professor{" +
                "id=" + id +
                ", usuarioEmail='" + usuarioEmail + '\'' +
                ", cargo='" + cargo + '\'' +
                ", tipoTG='" + tipoTG + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
