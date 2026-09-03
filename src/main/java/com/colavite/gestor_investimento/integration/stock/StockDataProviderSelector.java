package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class StockDataProviderSelector {

    private final Map<Mercado, StockDataProvider> providers;

    public StockDataProviderSelector(List<StockDataProvider> providers) {
        Map<Mercado, StockDataProvider> indexed = new EnumMap<>(Mercado.class);
        for (StockDataProvider provider : providers) {
            StockDataProvider previous = indexed.putIfAbsent(provider.mercado(), provider);
            if (previous != null) {
                throw new IllegalStateException("Há mais de um provider de ações para o mercado " + provider.mercado());
            }
        }
        this.providers = Map.copyOf(indexed);
    }

    public StockDataProvider para(Mercado mercado) {
        StockDataProvider provider = providers.get(mercado);
        if (provider == null) {
            throw new StockProviderUnavailableException();
        }
        return provider;
    }
}
