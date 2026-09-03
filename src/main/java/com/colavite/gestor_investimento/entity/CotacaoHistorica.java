package com.colavite.gestor_investimento.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cotacoes_historicas")
public class CotacaoHistorica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acao_id", nullable = false)
    private Acao acao;
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal cotacao;
    @Column(name = "data_hora_cotacao", nullable = false)
    private Instant dataHoraCotacao;
    @Column(name = "data_registro", nullable = false)
    private Instant dataRegistro;

    protected CotacaoHistorica() { }

    public CotacaoHistorica(Acao acao, BigDecimal cotacao, Instant dataHoraCotacao, Instant dataRegistro) {
        this.acao = acao;
        this.cotacao = cotacao;
        this.dataHoraCotacao = dataHoraCotacao;
        this.dataRegistro = dataRegistro;
    }
    public Long getId() { return id; }
    public Acao getAcao() { return acao; }
    public BigDecimal getCotacao() { return cotacao; }
    public Instant getDataHoraCotacao() { return dataHoraCotacao; }
    public Instant getDataRegistro() { return dataRegistro; }
}
