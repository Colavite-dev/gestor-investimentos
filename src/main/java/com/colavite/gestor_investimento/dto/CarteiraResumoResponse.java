package com.colavite.gestor_investimento.dto;
import com.colavite.gestor_investimento.entity.Moeda;
import java.util.Map;
public record CarteiraResumoResponse(Long carteiraId, Map<Moeda, ResumoMoedaResponse> porMoeda) {}
