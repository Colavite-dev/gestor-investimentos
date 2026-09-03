package com.colavite.gestor_investimento.integration.stock.twelvedata;

import java.util.List;

record TwelveDataSymbolSearchResponse(List<Result> data, String status, Integer code) {
    record Result(
            String symbol,
            String instrument_name,
            String instrument_type,
            String country,
            String currency
    ) {
    }
}
