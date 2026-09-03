package com.colavite.gestor_investimento.dto;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;

import java.math.BigDecimal;
import java.time.Instant;

public record AcaoResponse(Long id, String ticker, String nomeEmpresa, Mercado mercado, Moeda moeda,
                           BigDecimal cotacaoAtual, Instant dataHoraCotacao) {
}
