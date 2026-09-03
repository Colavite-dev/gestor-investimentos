package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Moeda;
import java.math.BigDecimal;
import java.time.Instant;

public record StockRegistrationData(String ticker, String nomeEmpresa, Moeda moeda, BigDecimal cotacaoAtual,
                                    Instant dataHoraCotacao) {
}
