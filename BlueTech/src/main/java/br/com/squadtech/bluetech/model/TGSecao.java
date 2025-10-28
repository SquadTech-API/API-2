package br.com.squadtech.bluetech.model;

import br.com.squadtech.bluetech.config.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TGSecao {

    private Long id;
    private Long portifolioId; // FK para TGPortifolio
    private Integer apiNumero; // 1..6
    private String status; // "PENDENTE", "CONCLUIDA"
    private Boolean versaoValidada;
    private LocalDateTime dataValidacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TGSecao() {}

    // Construtor completo e correto
    public TGSecao(Long portifolioId, Integer apiNumero, String status, Boolean versaoValidada) {
        this.portifolioId = portifolioId;
        this.apiNumero = apiNumero;
        this.status = status;
        this.versaoValidada = versaoValidada;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPortifolioId() { return portifolioId; }
    public void setPortifolioId(Long portifolioId) { this.portifolioId = portifolioId; }

    public Integer getApiNumero() { return apiNumero; }
    public void setApiNumero(Integer apiNumero) { this.apiNumero = apiNumero; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getVersaoValidada() { return versaoValidada; }
    public void setVersaoValidada(Boolean versaoValidada) { this.versaoValidada = versaoValidada; }

    public LocalDateTime getDataValidacao() { return dataValidacao; }
    public void setDataValidacao(LocalDateTime dataValidacao) { this.dataValidacao = dataValidacao; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "TGSecao{" +
                "id=" + id +
                ", portifolioId=" + portifolioId +
                ", apiNumero=" + apiNumero +
                ", status='" + status + '\'' +
                ", versaoValidada=" + versaoValidada +
                ", dataValidacao=" + dataValidacao +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }



}
