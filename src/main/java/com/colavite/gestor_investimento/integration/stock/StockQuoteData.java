package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Moeda;

import java.math.BigDecimal;
import java.time.Instant;

public record StockQuoteData(String ticker, Moeda moeda, BigDecimal cotacaoAtual, Instant dataHoraCotacao) {
}
