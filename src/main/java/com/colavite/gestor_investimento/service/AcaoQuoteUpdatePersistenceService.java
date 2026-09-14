package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.AcaoDuplicadaException;
import com.colavite.gestor_investimento.exception.AcaoNotFoundException;
import com.colavite.gestor_investimento.exception.AcaoResolutionRaceException;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import com.colavite.gestor_investimento.mapper.AcaoMapper;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
    public Acao cadastrar(StockRegistrationData data, Mercado mercado) {
        if (repository.existsByTickerAndMercado(data.ticker(), mercado)) {
            throw new AcaoDuplicadaException(data.ticker());
        }
        Acao acao;
        try {
            acao = repository.saveAndFlush(AcaoMapper.toEntity(data, mercado));
        } catch (DataIntegrityViolationException exception) {
            throw new AcaoDuplicadaException(data.ticker());
        }
        historico.registrar(acao, data.cotacaoAtual(), data.dataHoraCotacao());
        return acao;
    }

    @Transactional
    public Acao resolver(StockRegistrationData data, Mercado mercado) {
        try {
            Acao acao = repository.saveAndFlush(AcaoMapper.toEntity(data, mercado));
            historico.registrar(acao, data.cotacaoAtual(), data.dataHoraCotacao());
            return acao;
        } catch (DataIntegrityViolationException exception) {
            throw new AcaoResolutionRaceException(exception);
        }
    }

    @Transactional
    public Acao atualizar(Long id, StockQuoteData quote) {
        Acao acao = repository.findById(id).orElseThrow(() -> AcaoNotFoundException.porId(id));
        acao.atualizarCotacao(quote.cotacaoAtual(), quote.dataHoraCotacao());
        historico.registrar(acao, quote.cotacaoAtual(), quote.dataHoraCotacao());
        return acao;
    }
}
