package com.colavite.gestor_investimento.integration.stock.brapi;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

record BrapiStockQuoteResponse(List<Result> results) {
    record Result(String requestedSymbol, String symbol, Boolean changed, Data data) { }
    record Data(String shortName, String currency, BigDecimal regularMarketPrice, Instant regularMarketTime) { }
}
