package com.colavite.gestor_investimento.dto;

import java.util.List;

public record AcaoCatalogPageResponse(
        List<AcaoCatalogItemResponse> items,
        int page,
        int size,
        boolean hasNext,
        Long totalElements
) {
}
