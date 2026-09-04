package com.colavite.gestor_investimento.integration.stock.twelvedata;

record TwelveDataQuoteResponse(
        String symbol,
        String name,
        String currency,
        String close,
        Long timestamp,
        String status,
        Integer code,
        String message
) {
}
