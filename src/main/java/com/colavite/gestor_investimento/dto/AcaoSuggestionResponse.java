package com.colavite.gestor_investimento.dto;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;

public record AcaoSuggestionResponse(String ticker, String nomeEmpresa, Mercado mercado, Moeda moeda) { }
