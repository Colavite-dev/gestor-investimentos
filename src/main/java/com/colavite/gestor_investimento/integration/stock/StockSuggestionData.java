package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;

public record StockSuggestionData(String ticker, String nomeEmpresa, Mercado mercado, Moeda moeda) { }
