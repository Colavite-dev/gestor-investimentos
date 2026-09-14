package com.colavite.gestor_investimento.mapper;
import com.colavite.gestor_investimento.dto.OperacaoResponse;
import com.colavite.gestor_investimento.entity.Operacao;
public final class OperacaoMapper {
    private OperacaoMapper() {}
    public static OperacaoResponse toResponse(Operacao o) { return new OperacaoResponse(o.getId(), o.getCarteira().getId(), o.getAcao().getId(), o.getAcao().getTicker(), o.getAcao().getMoeda(), o.getTipo(), o.getQuantidade(), o.getPrecoUnitario(), o.getDataOperacao()); }
}
