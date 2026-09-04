package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.AcaoRequest;
import com.colavite.gestor_investimento.dto.AcaoResponse;
import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.AcaoNotFoundException;
import com.colavite.gestor_investimento.exception.TickerAmbiguoException;
import com.colavite.gestor_investimento.integration.stock.StockDataProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class AcaoService {
    private final AcaoRepository repository;
    private final StockDataProviderSelector stockDataProviderSelector;
    private final AcaoQuoteUpdatePersistenceService quoteUpdatePersistenceService;
    public AcaoService(AcaoRepository repository, StockDataProviderSelector stockDataProviderSelector, AcaoQuoteUpdatePersistenceService quoteUpdatePersistenceService) { this.repository = repository; this.stockDataProviderSelector = stockDataProviderSelector; this.quoteUpdatePersistenceService = quoteUpdatePersistenceService; }
    public AcaoResponse cadastrar(AcaoRequest request) {
        Mercado mercado = request.mercado();
        StockRegistrationData data = stockDataProviderSelector.para(mercado).consultar(normalizarTicker(request.ticker()));
        if (data == null || data.moeda() != mercado.moeda() || !cotacaoRepresentavel(data.cotacaoAtual()) || data.dataHoraCotacao() == null) {
            throw new com.colavite.gestor_investimento.exception.InvalidStockDataResponseException();
        }
        return com.colavite.gestor_investimento.mapper.AcaoMapper.toResponse(quoteUpdatePersistenceService.cadastrar(data, mercado));
    }
    @org.springframework.transaction.annotation.Transactional(readOnly = true) public List<AcaoResponse> listar() { return repository.findAllByOrderByIdAsc().stream().map(com.colavite.gestor_investimento.mapper.AcaoMapper::toResponse).toList(); }
    @org.springframework.transaction.annotation.Transactional(readOnly = true) public AcaoResponse buscarPorId(Long id) { return repository.findById(id).map(com.colavite.gestor_investimento.mapper.AcaoMapper::toResponse).orElseThrow(() -> AcaoNotFoundException.porId(id)); }
    @org.springframework.transaction.annotation.Transactional(readOnly = true) public AcaoResponse buscarPorTicker(String ticker, Mercado mercado) {
        String normalizedTicker = normalizarTicker(ticker);
        if (mercado != null) return repository.findByTickerAndMercado(normalizedTicker, mercado).map(com.colavite.gestor_investimento.mapper.AcaoMapper::toResponse).orElseThrow(() -> AcaoNotFoundException.porTicker(normalizedTicker));
        List<Acao> found = repository.findByTicker(normalizedTicker);
        if (found.isEmpty()) throw AcaoNotFoundException.porTicker(normalizedTicker);
        if (found.size() > 1) throw new TickerAmbiguoException(normalizedTicker);
        return com.colavite.gestor_investimento.mapper.AcaoMapper.toResponse(found.get(0));
    }
    public AcaoResponse atualizarCotacao(Long id) {
        Acao acao = repository.findById(id).orElseThrow(() -> AcaoNotFoundException.porId(id));
        StockQuoteData quote = stockDataProviderSelector.para(acao.getMercado()).consultarCotacao(acao.getTicker());
        validarCotacao(acao, quote);
        return com.colavite.gestor_investimento.mapper.AcaoMapper.toResponse(quoteUpdatePersistenceService.atualizar(id, quote));
    }
    private void validarCotacao(Acao acao, StockQuoteData quote) {
        if (quote == null || quote.ticker() == null || !acao.getTicker().equals(normalizarTicker(quote.ticker())) || quote.moeda() != acao.getMoeda()
                || !cotacaoRepresentavel(quote.cotacaoAtual()) || quote.dataHoraCotacao() == null) {
            throw new com.colavite.gestor_investimento.exception.InvalidStockDataResponseException();
        }
    }
    private boolean cotacaoRepresentavel(BigDecimal cotacao) {
        return cotacao != null && cotacao.signum() > 0 && cotacao.scale() <= 8 && cotacao.precision() - cotacao.scale() <= 11;
    }
    public String normalizarTicker(String ticker) { return ticker.trim().toUpperCase(Locale.ROOT); }
}
