package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.exception.AcaoNotFoundException;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AcaoQuoteUpdatePersistenceService {

    private final AcaoRepository repository;
    private final CotacaoHistoricaService historico;

    AcaoQuoteUpdatePersistenceService(AcaoRepository repository, CotacaoHistoricaService historico) {
        this.repository = repository;
        this.historico = historico;
    }

    @Transactional
    public Acao atualizar(Long id, StockQuoteData quote) {
        Acao acao = repository.findById(id).orElseThrow(() -> AcaoNotFoundException.porId(id));
        acao.atualizarCotacao(quote.cotacaoAtual(), quote.dataHoraCotacao());
        historico.registrar(acao, quote.cotacaoAtual(), quote.dataHoraCotacao());
        return acao;
    }
}
