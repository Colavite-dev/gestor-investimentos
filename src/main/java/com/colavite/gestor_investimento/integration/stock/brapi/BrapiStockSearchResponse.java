package com.colavite.gestor_investimento.integration.stock.brapi;

import java.math.BigDecimal;
import java.util.List;

record BrapiStockSearchResponse(
        List<Result> stocks,
        Integer currentPage,
        Integer totalPages,
        Integer itemsPerPage,
        Long totalCount,
        Boolean hasNextPage
) {
    record Result(
            String stock,
            String symbol,
            String name,
            String shortName,
            String type,
            String assetType,
            String currency,
            BigDecimal close,
            String logo
    ) {
    }
}
