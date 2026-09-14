package com.colavite.gestor_investimento.dto;
import com.colavite.gestor_investimento.entity.TipoOperacao;
import com.colavite.gestor_investimento.entity.Moeda;
import java.math.BigDecimal;
import java.time.Instant;
public record OperacaoResponse(Long id, Long carteiraId, Long acaoId, String ticker, Moeda moeda, TipoOperacao tipo, BigDecimal quantidade, BigDecimal precoUnitario, Instant dataOperacao) {}
