package com.colavite.gestor_investimento.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "operacoes")
public class Operacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "carteira_id", nullable = false) private Carteira carteira;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "acao_id", nullable = false) private Acao acao;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private TipoOperacao tipo;
    @Column(nullable = false, precision = 19, scale = 8) private BigDecimal quantidade;
    @Column(name = "preco_unitario", nullable = false, precision = 19, scale = 8) private BigDecimal precoUnitario;
    @Column(name = "data_operacao", nullable = false) private Instant dataOperacao;
    protected Operacao() {}
    public Operacao(Carteira carteira, Acao acao, TipoOperacao tipo, BigDecimal quantidade, BigDecimal precoUnitario, Instant dataOperacao) {
        this.carteira = carteira; this.acao = acao; this.tipo = tipo; this.quantidade = quantidade; this.precoUnitario = precoUnitario; this.dataOperacao = dataOperacao;
    }
    public Long getId() { return id; }
    public Carteira getCarteira() { return carteira; }
    public Acao getAcao() { return acao; }
    public TipoOperacao getTipo() { return tipo; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public Instant getDataOperacao() { return dataOperacao; }
}
