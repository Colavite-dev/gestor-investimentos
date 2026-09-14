package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Mercado;
import java.util.List;

public interface StockDataProvider {
    Mercado mercado();

    StockRegistrationData consultar(String ticker);

    List<StockSuggestionData> pesquisar(String termo);

    StockQuoteData consultarCotacao(String ticker);
}
