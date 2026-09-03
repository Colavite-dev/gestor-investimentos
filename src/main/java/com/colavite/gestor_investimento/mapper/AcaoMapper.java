package com.colavite.gestor_investimento.mapper;

import com.colavite.gestor_investimento.dto.AcaoResponse;
import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;

public final class AcaoMapper {
    private AcaoMapper() { }
    public static Acao toEntity(StockRegistrationData data, Mercado mercado) { return new Acao(data.ticker(), data.nomeEmpresa(), mercado, data.cotacaoAtual(), data.dataHoraCotacao()); }
    public static AcaoResponse toResponse(Acao acao) { return new AcaoResponse(acao.getId(), acao.getTicker(), acao.getNomeEmpresa(), acao.getMercado(), acao.getMoeda(), acao.getCotacaoAtual(), acao.getDataHoraCotacao()); }
}
