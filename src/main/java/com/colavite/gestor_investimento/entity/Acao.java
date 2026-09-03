package com.colavite.gestor_investimento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "acoes", uniqueConstraints = @UniqueConstraint(name = "uk_acoes_ticker_mercado", columnNames = {"ticker", "mercado"}))
public class Acao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(name = "nome_empresa", nullable = false, length = 150)
    private String nomeEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Mercado mercado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Moeda moeda;

    @Column(name = "cotacao_atual", nullable = false, precision = 19, scale = 4)
    private BigDecimal cotacaoAtual;

    @Column(name = "data_hora_cotacao", nullable = false)
    private Instant dataHoraCotacao;

    protected Acao() {
    }

    public Acao(String ticker, String nomeEmpresa, Mercado mercado, BigDecimal cotacaoAtual, Instant dataHoraCotacao) {
        this.ticker = ticker;
        this.nomeEmpresa = nomeEmpresa;
        this.mercado = mercado;
        this.moeda = mercado.moeda();
        this.cotacaoAtual = cotacaoAtual;
        this.dataHoraCotacao = dataHoraCotacao;
    }

    public Long getId() { return id; }
    public String getTicker() { return ticker; }
    public String getNomeEmpresa() { return nomeEmpresa; }
    public Mercado getMercado() { return mercado; }
    public Moeda getMoeda() { return moeda; }
    public BigDecimal getCotacaoAtual() { return cotacaoAtual; }
    public Instant getDataHoraCotacao() { return dataHoraCotacao; }

    public void atualizarCotacao(BigDecimal cotacaoAtual, Instant dataHoraCotacao) {
        this.cotacaoAtual = cotacaoAtual;
        this.dataHoraCotacao = dataHoraCotacao;
    }
}
