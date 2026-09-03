package com.colavite.gestor_investimento.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CotacaoHistoricaResponse(Long id, BigDecimal cotacao, Instant dataHoraCotacao, Instant dataRegistro) { }
