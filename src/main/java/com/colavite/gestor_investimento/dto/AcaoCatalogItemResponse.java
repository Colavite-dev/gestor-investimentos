package com.colavite.gestor_investimento.dto;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;

import java.math.BigDecimal;

public record AcaoCatalogItemResponse(
        String ticker,
        String nomeEmpresa,
        Mercado mercado,
        Moeda moeda,
        String exchange,
        String micCode,
        BigDecimal cotacaoAtual,
        String logoUrl
) {
}
