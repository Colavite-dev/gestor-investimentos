package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.AcaoRequest;
import com.colavite.gestor_investimento.dto.AcaoResponse;
import com.colavite.gestor_investimento.dto.AcaoResolveRequest;
import com.colavite.gestor_investimento.dto.AcaoSuggestionResponse;
import com.colavite.gestor_investimento.dto.AcaoCatalogItemResponse;
import com.colavite.gestor_investimento.dto.AcaoCatalogPageResponse;
import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.AcaoNotFoundException;
import com.colavite.gestor_investimento.exception.TickerAmbiguoException;
import com.colavite.gestor_investimento.integration.stock.StockDataProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockCatalogProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import com.colavite.gestor_investimento.integration.stock.StockSuggestionData;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

@Service
public class AcaoService {
    private final AcaoRepository repository;
    private final StockDataProviderSelector stockDataProviderSelector;
    private final StockCatalogProviderSelector stockCatalogProviderSelector;
    private final AcaoQuoteUpdatePersistenceService quoteUpdatePersistenceService;
    public AcaoService(AcaoRepository repository, StockDataProviderSelector stockDataProviderSelector, StockCatalogProviderSelector stockCatalogProviderSelector, AcaoQuoteUpdatePersistenceService quoteUpdatePersistenceService) { this.repository = repository; this.stockDataProviderSelector = stockDataProviderSelector; this.stockCatalogProviderSelector = stockCatalogProviderSelector; this.quoteUpdatePersistenceService = quoteUpdatePersistenceService; }
    public AcaoResponse cadastrar(AcaoRequest request) {
        Mercado mercado = request.mercado();
        StockRegistrationData data = stockDataProviderSelector.para(mercado).consultar(normalizarTicker(request.ticker()));
        if (data == null || data.moeda() != mercado.moeda() || !cotacaoRepresentavel(data.cotacaoAtual()) || data.dataHoraCotacao() == null) {
            throw new com.colavite.gestor_investimento.exception.InvalidStockDataResponseException();
        }
        return com.colavite.gestor_investimento.mapper.AcaoMapper.toResponse(quoteUpdatePersistenceService.cadastrar(data, mercado));
    }
    public List<AcaoSuggestionResponse> pesquisar(String termo) {
        String normalized = normalizarTermo(termo);
        BuscaProvider br = buscar(Mercado.BRASIL, normalized);
        BuscaProvider us = buscar(Mercado.ESTADOS_UNIDOS, normalized);
        if (!br.utilizavel() && !us.utilizavel()) {
            if (br.indisponivel() || us.indisponivel()) throw new com.colavite.gestor_investimento.exception.StockProviderUnavailableException();
            throw new com.colavite.gestor_investimento.exception.InvalidStockDataResponseException();
        }
        return java.util.stream.Stream.concat(br.resultados().stream(), us.resultados().stream())
                .filter(this::sugestaoValida)
                .collect(java.util.stream.Collectors.toMap(s -> normalizarTicker(s.ticker()) + "|" + s.mercado(), Function.identity(), (a, b) -> a))
                .values().stream().sorted(comparador(normalized)).limit(12)
                .map(s -> new AcaoSuggestionResponse(normalizarTicker(s.ticker()), s.nomeEmpresa(), s.mercado(), s.moeda())).toList();
    }
    public AcaoCatalogPageResponse catalogar(Mercado mercado, String termo, int page, int size) {
        var result = stockCatalogProviderSelector.para(mercado).catalogar(termo == null ? "" : termo.trim(), page, size);
        if (result == null || result.items() == null || result.page() != page || result.size() != size) {
            throw new com.colavite.gestor_investimento.exception.InvalidStockDataResponseException();
        }
        List<AcaoCatalogItemResponse> items = result.items().stream()
                .filter(item -> item != null && item.mercado() == mercado && item.moeda() == mercado.moeda()
                        && item.ticker() != null && !item.ticker().isBlank())
                .map(item -> new AcaoCatalogItemResponse(normalizarTicker(item.ticker()), item.nomeEmpresa(), item.mercado(),
                        item.moeda(), item.exchange(), item.micCode(), item.cotacaoAtual(),
                        mercado == Mercado.BRASIL ? sanitizarLogoHttps(item.logoUrl()) : null))
                .toList();
        return new AcaoCatalogPageResponse(items, page, size, result.hasNext(), result.totalElements());
    }
    public AcaoResponse resolver(AcaoResolveRequest request) {
        String ticker = normalizarTicker(request.ticker());
        Mercado mercado = request.mercado();
        Optional<Acao> existente = repository.findByTickerAndMercado(ticker, mercado);
        if (existente.isPresent()) return com.colavite.gestor_investimento.mapper.AcaoMapper.toResponse(existente.get());
        StockRegistrationData data = stockDataProviderSelector.para(mercado).consultar(ticker);
        validarCadastro(data, mercado);
        try {
            return com.colavite.gestor_investimento.mapper.AcaoMapper.toResponse(quoteUpdatePersistenceService.resolver(data, mercado));
        } catch (com.colavite.gestor_investimento.exception.AcaoResolutionRaceException exception) {
            return repository.findByTickerAndMercado(ticker, mercado)
                    .map(com.colavite.gestor_investimento.mapper.AcaoMapper::toResponse)
                    .orElseThrow(() -> exception);
        }
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
    private void validarCadastro(StockRegistrationData data, Mercado mercado) {
        if (data == null || data.moeda() != mercado.moeda() || !cotacaoRepresentavel(data.cotacaoAtual()) || data.dataHoraCotacao() == null) {
            throw new com.colavite.gestor_investimento.exception.InvalidStockDataResponseException();
        }
    }
    private BuscaProvider buscar(Mercado mercado, String termo) {
        try { return BuscaProvider.sucesso(stockDataProviderSelector.para(mercado).pesquisar(termo));
        } catch (com.colavite.gestor_investimento.exception.StockProviderUnavailableException exception) { return BuscaProvider.falhaIndisponibilidade();
        } catch (com.colavite.gestor_investimento.exception.StockTickerNotFoundException exception) { return BuscaProvider.sucesso(List.of());
        } catch (com.colavite.gestor_investimento.exception.InvalidStockDataResponseException exception) { return BuscaProvider.falhaPayload(); }
    }
    private boolean sugestaoValida(StockSuggestionData sugestao) {
        return sugestao != null && sugestao.ticker() != null && !sugestao.ticker().isBlank()
                && sugestao.mercado() != null && sugestao.moeda() == sugestao.mercado().moeda();
    }
    private Comparator<StockSuggestionData> comparador(String termo) {
        return Comparator.comparingInt((StockSuggestionData s) -> {
            String ticker = normalizarTicker(s.ticker()); String nome = s.nomeEmpresa() == null ? "" : s.nomeEmpresa().toUpperCase(Locale.ROOT);
            if (ticker.equals(termo)) return 0; if (ticker.startsWith(termo)) return 1; if (nome.contains(termo)) return 2; return 3;
        }).thenComparing(s -> s.mercado().name()).thenComparing(s -> normalizarTicker(s.ticker()));
    }
    private String normalizarTermo(String termo) { return termo.trim().toUpperCase(Locale.ROOT); }
    private String sanitizarLogoHttps(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            URI uri = new URI(value.trim());
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null
                    ? uri.toASCIIString() : null;
        } catch (URISyntaxException exception) {
            return null;
        }
    }
    private record BuscaProvider(List<StockSuggestionData> resultados, boolean utilizavel, boolean indisponivel) {
        static BuscaProvider sucesso(List<StockSuggestionData> results) { return new BuscaProvider(results == null ? List.of() : results, true, false); }
        static BuscaProvider falhaIndisponibilidade() { return new BuscaProvider(List.of(), false, true); }
        static BuscaProvider falhaPayload() { return new BuscaProvider(List.of(), false, false); }
    }
    public String normalizarTicker(String ticker) { return ticker.trim().toUpperCase(Locale.ROOT); }
}
