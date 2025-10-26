package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

public class Orienta {

    private Long id;
    private Long professorId;
    private Long alunoId;
    private boolean ativo;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor padrão
    public Orienta() {}

    // Construtor completo
    public Orienta(Long id, Long professorId, Long alunoId, boolean ativo,
                   LocalDateTime dataInicio, LocalDateTime dataFim,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.professorId = professorId;
        this.alunoId = alunoId;
        this.ativo = ativo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Construtor simplificado (para criação inicial)
    public Orienta(Long professorId, Long alunoId) {
        this.professorId = professorId;
        this.alunoId = alunoId;
        this.ativo = true;
        this.dataInicio = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Orienta{" +
                "id=" + id +
                ", professorId=" + professorId +
                ", alunoId=" + alunoId +
                ", ativo=" + ativo +
                ", dataInicio=" + dataInicio +
                ", dataFim=" + dataFim +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
