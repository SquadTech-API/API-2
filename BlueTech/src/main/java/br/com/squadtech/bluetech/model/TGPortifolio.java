package br.com.squadtech.bluetech.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TGPortifolio {

    private Long id;
    private Long alunoId;               // Apenas o aluno
    private String titulo;
    private String tema;
    private String status;              // "EM_ANDAMENTO", "CONCLUIDO"
    private BigDecimal percentualConclusao;
    private String conteudoFinal;
    private LocalDateTime dataInicio;
    private LocalDateTime dataConclusao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TGPortifolio() {}

    public TGPortifolio(Long alunoId, String titulo, String tema, String status,
                        BigDecimal percentualConclusao, String conteudoFinal) {
        this.alunoId = alunoId;
        this.titulo = titulo;
        this.tema = tema;
        this.status = status;
        this.percentualConclusao = percentualConclusao;
        this.conteudoFinal = conteudoFinal;
    }

    // ---------- Getters e Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getPercentualConclusao() { return percentualConclusao; }
    public void setPercentualConclusao(BigDecimal percentualConclusao) { this.percentualConclusao = percentualConclusao; }

    public String getConteudoFinal() { return conteudoFinal; }
    public void setConteudoFinal(String conteudoFinal) { this.conteudoFinal = conteudoFinal; }

    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "TGPortifolio{" +
                "id=" + id +
                ", alunoId=" + alunoId +
                ", titulo='" + titulo + '\'' +
                ", tema='" + tema + '\'' +
                ", status='" + status + '\'' +
                ", percentualConclusao=" + percentualConclusao +
                ", conteudoFinal='" + conteudoFinal + '\'' +
                ", dataInicio=" + dataInicio +
                ", dataConclusao=" + dataConclusao +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
