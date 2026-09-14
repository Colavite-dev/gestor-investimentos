package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class StockCatalogProviderSelector {
    private final Map<Mercado, StockCatalogProvider> providers;

    public StockCatalogProviderSelector(List<StockCatalogProvider> providers) {
        Map<Mercado, StockCatalogProvider> indexed = new EnumMap<>(Mercado.class);
        for (StockCatalogProvider provider : providers) {
            StockCatalogProvider previous = indexed.putIfAbsent(provider.mercado(), provider);
            if (previous != null) {
                throw new IllegalStateException("Há mais de um provider de catálogo para o mercado " + provider.mercado());
            }
        }
        this.providers = Map.copyOf(indexed);
    }

    public StockCatalogProvider para(Mercado mercado) {
        StockCatalogProvider provider = providers.get(mercado);
        if (provider == null) {
            throw new StockProviderUnavailableException();
        }
        return provider;
    }
}
