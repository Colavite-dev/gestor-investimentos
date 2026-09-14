package com.colavite.gestor_investimento.integration.stock.twelvedata;

import java.util.List;

record TwelveDataStocksResponse(Integer count, List<Result> data, String status, Integer code, String message) {
    record Result(
            String symbol,
            String name,
            String currency,
            String exchange,
            String mic_code,
            String country,
            String type
    ) {
    }
}
