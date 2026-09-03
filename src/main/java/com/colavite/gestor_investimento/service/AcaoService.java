package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.AcaoRequest;
import com.colavite.gestor_investimento.dto.AcaoResponse;
import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.AcaoDuplicadaException;
import com.colavite.gestor_investimento.exception.AcaoNotFoundException;
import com.colavite.gestor_investimento.exception.TickerAmbiguoException;
import com.colavite.gestor_investimento.integration.stock.StockDataProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import com.colavite.gestor_investimento.mapper.AcaoMapper;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Locale;

@Service
public class AcaoService {
    private final AcaoRepository repository;
    private final StockDataProviderSelector stockDataProviderSelector;
    private final AcaoQuoteUpdatePersistenceService quoteUpdatePersistenceService;
    private final CotacaoHistoricaService cotacaoHistoricaService;
    @Autowired public AcaoService(AcaoRepository repository, StockDataProviderSelector stockDataProviderSelector, AcaoQuoteUpdatePersistenceService quoteUpdatePersistenceService, CotacaoHistoricaService cotacaoHistoricaService) { this.repository = repository; this.stockDataProviderSelector = stockDataProviderSelector; this.quoteUpdatePersistenceService = quoteUpdatePersistenceService; this.cotacaoHistoricaService = cotacaoHistoricaService; }
    AcaoService(AcaoRepository repository, StockDataProviderSelector selector, AcaoQuoteUpdatePersistenceService persistence) { this(repository, selector, persistence, null); }
    @Transactional public AcaoResponse cadastrar(AcaoRequest request) {
        Mercado mercado = request.mercado();
        StockRegistrationData data = stockDataProviderSelector.para(mercado).consultar(normalizarTicker(request.ticker()));
        if (data.moeda() != mercado.moeda()) {
            throw new com.colavite.gestor_investimento.exception.InvalidStockDataResponseException();
        }
        if (repository.existsByTickerAndMercado(data.ticker(), mercado)) throw new AcaoDuplicadaException(data.ticker());
        try { Acao acao = repository.saveAndFlush(AcaoMapper.toEntity(data, mercado)); if (cotacaoHistoricaService != null) cotacaoHistoricaService.registrar(acao, data.cotacaoAtual(), data.dataHoraCotacao()); return AcaoMapper.toResponse(acao); }
        catch (DataIntegrityViolationException e) { throw new AcaoDuplicadaException(data.ticker()); }
    }
    @Transactional(readOnly = true) public List<AcaoResponse> listar() { return repository.findAllByOrderByIdAsc().stream().map(AcaoMapper::toResponse).toList(); }
    @Transactional(readOnly = true) public AcaoResponse buscarPorId(Long id) { return repository.findById(id).map(AcaoMapper::toResponse).orElseThrow(() -> AcaoNotFoundException.porId(id)); }
    @Transactional(readOnly = true) public AcaoResponse buscarPorTicker(String ticker, Mercado mercado) {
        String normalizedTicker = normalizarTicker(ticker);
        if (mercado != null) return repository.findByTickerAndMercado(normalizedTicker, mercado).map(AcaoMapper::toResponse).orElseThrow(() -> AcaoNotFoundException.porTicker(normalizedTicker));
        List<Acao> found = repository.findByTicker(normalizedTicker);
        if (found.isEmpty()) throw AcaoNotFoundException.porTicker(normalizedTicker);
        if (found.size() > 1) throw new TickerAmbiguoException(normalizedTicker);
        return AcaoMapper.toResponse(found.get(0));
    }
    public AcaoResponse atualizarCotacao(Long id) {
        Acao acao = repository.findById(id).orElseThrow(() -> AcaoNotFoundException.porId(id));
        StockQuoteData quote = stockDataProviderSelector.para(acao.getMercado()).consultarCotacao(acao.getTicker());
        validarCotacao(acao, quote);
        return AcaoMapper.toResponse(quoteUpdatePersistenceService.atualizar(id, quote));
    }
    private void validarCotacao(Acao acao, StockQuoteData quote) {
        if (quote == null || quote.ticker() == null || !acao.getTicker().equals(normalizarTicker(quote.ticker())) || quote.moeda() != acao.getMoeda()
                || quote.cotacaoAtual() == null || quote.cotacaoAtual().signum() <= 0 || quote.dataHoraCotacao() == null) {
            throw new com.colavite.gestor_investimento.exception.InvalidStockDataResponseException();
        }
    }
    public String normalizarTicker(String ticker) { return ticker.trim().toUpperCase(Locale.ROOT); }
}
