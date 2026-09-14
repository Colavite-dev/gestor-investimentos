package com.colavite.gestor_investimento.dto;

import java.util.List;

public record CarteiraQuoteRefreshResponse(
        int quantidadeAtualizada,
        int quantidadeComFalha,
        List<String> tickersComFalha
) {
}
