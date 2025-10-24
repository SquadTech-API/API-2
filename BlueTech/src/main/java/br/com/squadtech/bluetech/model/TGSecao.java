package br.com.squadtech.bluetech.model;

import java.time.LocalDateTime;

public class TGSecao {
    private Integer idSecao;
    private Integer apiNumero; // 1..6
    private LocalDateTime dataEnvio;
    private LocalDateTime dataAprovacao; // pode ser null
    private String status; // Aprovada, Revisar, Aguardando Feedback
    private Integer idVersao; // FK para TG_Versao

    public TGSecao() {}

    public TGSecao(Integer apiNumero, LocalDateTime dataEnvio, String status, Integer idVersao) {
        this.apiNumero = apiNumero;
        this.dataEnvio = dataEnvio;
        this.status = status;
        this.idVersao = idVersao;
    }

    public Integer getIdSecao() { return idSecao; }
    public void setIdSecao(Integer idSecao) { this.idSecao = idSecao; }

    public Integer getApiNumero() { return apiNumero; }
    public void setApiNumero(Integer apiNumero) { this.apiNumero = apiNumero; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public LocalDateTime getDataAprovacao() { return dataAprovacao; }
    public void setDataAprovacao(LocalDateTime dataAprovacao) { this.dataAprovacao = dataAprovacao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getIdVersao() { return idVersao; }
    public void setIdVersao(Integer idVersao) { this.idVersao = idVersao; }
}
