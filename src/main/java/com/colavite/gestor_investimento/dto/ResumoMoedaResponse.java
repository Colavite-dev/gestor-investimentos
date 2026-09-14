package com.colavite.gestor_investimento.dto;
import java.math.BigDecimal;
public record ResumoMoedaResponse(int quantidadePosicoes, BigDecimal valorInvestido, BigDecimal patrimonioAtual,
        BigDecimal lucroPrejuizo, BigDecimal rentabilidadePercentual) {}
