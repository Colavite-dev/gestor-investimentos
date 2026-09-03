package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Mercado;

public interface StockDataProvider {
    Mercado mercado();

    StockRegistrationData consultar(String ticker);

    StockQuoteData consultarCotacao(String ticker);
}
