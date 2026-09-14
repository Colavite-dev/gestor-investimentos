package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CarteiraQuoteRefreshResponse;
import com.colavite.gestor_investimento.dto.PosicaoResponse;
import com.colavite.gestor_investimento.exception.CarteiraNotFoundException;
import com.colavite.gestor_investimento.repository.CarteiraRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarteiraQuoteRefreshService {
    private final CarteiraRepository carteiras;
    private final CarteiraPosicaoService posicoes;
    private final AcaoService acoes;

    public CarteiraQuoteRefreshService(CarteiraRepository carteiras, CarteiraPosicaoService posicoes, AcaoService acoes) {
        this.carteiras = carteiras;
        this.posicoes = posicoes;
        this.acoes = acoes;
    }

    public CarteiraQuoteRefreshResponse atualizarCotacoes(Long carteiraId, Long usuarioId) {
        carteiras.findByIdAndUsuarioId(carteiraId, usuarioId)
                .orElseThrow(() -> CarteiraNotFoundException.porId(carteiraId));

        Map<Long, String> ativosAbertos = new LinkedHashMap<>();
        for (PosicaoResponse posicao : posicoes.listarPosicoes(carteiraId, usuarioId)) {
            if (posicao.quantidade().signum() > 0) ativosAbertos.putIfAbsent(posicao.acaoId(), posicao.ticker());
        }

        int atualizadas = 0;
        List<String> falhas = new java.util.ArrayList<>();
        for (Map.Entry<Long, String> ativo : ativosAbertos.entrySet()) {
            try {
                acoes.atualizarCotacao(ativo.getKey());
                atualizadas++;
            } catch (RuntimeException exception) {
                falhas.add(ativo.getValue());
            }
        }
        return new CarteiraQuoteRefreshResponse(atualizadas, falhas.size(), List.copyOf(falhas));
    }
}
