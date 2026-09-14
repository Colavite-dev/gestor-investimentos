package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Mercado;

public interface StockCatalogProvider {
    Mercado mercado();

    StockCatalogPageData catalogar(String termo, int page, int size);
}
