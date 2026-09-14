package com.colavite.gestor_investimento.integration.stock;

import java.util.List;

public record StockCatalogPageData(
        List<StockCatalogItemData> items,
        int page,
        int size,
        boolean hasNext,
        Long totalElements
) {
    public StockCatalogPageData {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
