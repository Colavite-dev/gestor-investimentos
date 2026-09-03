package com.colavite.gestor_investimento.mapper;
import com.colavite.gestor_investimento.dto.CarteiraResponse;
import com.colavite.gestor_investimento.entity.Carteira;
public final class CarteiraMapper {
    private CarteiraMapper() {}
    public static CarteiraResponse toResponse(Carteira c) { return new CarteiraResponse(c.getId(), c.getNome(), c.getDescricao(), c.getDataCadastro()); }
}
