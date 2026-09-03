package com.colavite.gestor_investimento.dto;
import com.colavite.gestor_investimento.entity.TipoOperacao;
import java.math.BigDecimal;
import java.time.Instant;
public record OperacaoResponse(Long id, Long carteiraId, Long acaoId, String ticker, TipoOperacao tipo, BigDecimal quantidade, BigDecimal precoUnitario, Instant dataOperacao) {}
