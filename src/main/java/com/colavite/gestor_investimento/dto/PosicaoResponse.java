package com.colavite.gestor_investimento.dto;
import com.colavite.gestor_investimento.entity.*;
import java.math.BigDecimal;
public record PosicaoResponse(Long acaoId, String ticker, Mercado mercado, Moeda moeda,
        BigDecimal quantidade, BigDecimal precoMedio, BigDecimal valorInvestido,
        BigDecimal cotacaoAtual, BigDecimal patrimonioAtual, BigDecimal lucroPrejuizo,
        BigDecimal rentabilidadePercentual) {}
