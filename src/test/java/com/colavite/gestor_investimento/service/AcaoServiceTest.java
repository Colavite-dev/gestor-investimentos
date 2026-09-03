package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.AcaoRequest;
import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.exception.AcaoDuplicadaException;
import com.colavite.gestor_investimento.integration.stock.StockDataProvider;
import com.colavite.gestor_investimento.integration.stock.StockDataProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AcaoServiceTest {

    @Mock AcaoRepository repository;
    @Mock StockDataProviderSelector selector;
    @Mock StockDataProvider brazilProvider;
    @Mock StockDataProvider usProvider;
    @Mock AcaoQuoteUpdatePersistenceService quoteUpdatePersistenceService;
    AcaoService service;

    @BeforeEach
    void setup() {
        service = new AcaoService(repository, selector, quoteUpdatePersistenceService);
    }

    @Test
    void resolveDadosBrasileirosENormalizaTicker() {
        when(selector.para(Mercado.BRASIL)).thenReturn(brazilProvider);
        when(brazilProvider.consultar("PETR4")).thenReturn(data("PETR4", Moeda.BRL));
        when(quoteUpdatePersistenceService.cadastrar(any(), eq(Mercado.BRASIL))).thenReturn(acao("PETR4", Mercado.BRASIL));

        var result = service.cadastrar(new AcaoRequest(" petr4 ", Mercado.BRASIL));

        assertThat(result.ticker()).isEqualTo("PETR4");
        assertThat(result.mercado()).isEqualTo(Mercado.BRASIL);
        assertThat(result.moeda()).isEqualTo(Moeda.BRL);
        verify(brazilProvider).consultar("PETR4");
        verify(quoteUpdatePersistenceService).cadastrar(any(), eq(Mercado.BRASIL));
        verify(usProvider, never()).consultar(any());
    }

    @Test
    void resolveDadosAmericanosPeloProviderDoMercado() {
        when(selector.para(Mercado.ESTADOS_UNIDOS)).thenReturn(usProvider);
        when(usProvider.consultar("AAPL")).thenReturn(data("AAPL", Moeda.USD));
        when(quoteUpdatePersistenceService.cadastrar(any(), eq(Mercado.ESTADOS_UNIDOS))).thenReturn(acao("AAPL", Mercado.ESTADOS_UNIDOS));

        var result = service.cadastrar(new AcaoRequest(" aapl ", Mercado.ESTADOS_UNIDOS));

        assertThat(result.ticker()).isEqualTo("AAPL");
        assertThat(result.mercado()).isEqualTo(Mercado.ESTADOS_UNIDOS);
        assertThat(result.moeda()).isEqualTo(Moeda.USD);
        verify(usProvider).consultar("AAPL");
        verify(quoteUpdatePersistenceService).cadastrar(any(), eq(Mercado.ESTADOS_UNIDOS));
        verify(brazilProvider, never()).consultar(any());
    }

    @Test
    void usaTickerResolvidoERejeitaDuplicidadeNoMesmoMercado() {
        when(selector.para(Mercado.ESTADOS_UNIDOS)).thenReturn(usProvider);
        when(usProvider.consultar("AAPL")).thenReturn(data("AAPL", Moeda.USD));
        when(quoteUpdatePersistenceService.cadastrar(any(), eq(Mercado.ESTADOS_UNIDOS))).thenThrow(new AcaoDuplicadaException("AAPL"));

        assertThatThrownBy(() -> service.cadastrar(new AcaoRequest("aapl", Mercado.ESTADOS_UNIDOS)))
                .isInstanceOf(AcaoDuplicadaException.class);

        verify(quoteUpdatePersistenceService).cadastrar(any(), eq(Mercado.ESTADOS_UNIDOS));
    }

    @Test
    void atualizaSomenteCotacaoETimestampPeloProviderBrasileiro() {
        Acao acao = new Acao("PETR4", "Petrobras", Mercado.BRASIL, new BigDecimal("20.00"), Instant.parse("2026-09-01T12:00:00Z"));
        StockQuoteData quote = new StockQuoteData("PETR4", Moeda.BRL, new BigDecimal("21.25"), Instant.parse("2026-09-02T12:00:00Z"));
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(acao));
        when(selector.para(Mercado.BRASIL)).thenReturn(brazilProvider);
        when(brazilProvider.consultarCotacao("PETR4")).thenReturn(quote);
        when(quoteUpdatePersistenceService.atualizar(1L, quote)).thenAnswer(invocation -> {
            acao.atualizarCotacao(quote.cotacaoAtual(), quote.dataHoraCotacao());
            return acao;
        });

        var response = service.atualizarCotacao(1L);

        assertThat(response.cotacaoAtual()).isEqualByComparingTo("21.25");
        assertThat(response.dataHoraCotacao()).isEqualTo(Instant.parse("2026-09-02T12:00:00Z"));
        assertThat(response.ticker()).isEqualTo("PETR4");
        assertThat(response.nomeEmpresa()).isEqualTo("Petrobras");
        assertThat(response.mercado()).isEqualTo(Mercado.BRASIL);
        assertThat(response.moeda()).isEqualTo(Moeda.BRL);
        verify(brazilProvider).consultarCotacao("PETR4");
        verify(usProvider, never()).consultarCotacao(any());
    }

    @Test
    void atualizaAcaoAmericanaPeloProviderCorreto() {
        Acao acao = new Acao("AAPL", "Apple Inc.", Mercado.ESTADOS_UNIDOS, new BigDecimal("200.00"), Instant.parse("2026-09-01T12:00:00Z"));
        StockQuoteData quote = new StockQuoteData("AAPL", Moeda.USD, new BigDecimal("213.49"), Instant.parse("2026-09-02T12:00:00Z"));
        when(repository.findById(2L)).thenReturn(java.util.Optional.of(acao));
        when(selector.para(Mercado.ESTADOS_UNIDOS)).thenReturn(usProvider);
        when(usProvider.consultarCotacao("AAPL")).thenReturn(quote);
        when(quoteUpdatePersistenceService.atualizar(2L, quote)).thenReturn(acao);

        service.atualizarCotacao(2L);

        verify(usProvider).consultarCotacao("AAPL");
        verify(brazilProvider, never()).consultarCotacao(any());
    }

    @Test
    void naoConsultaProviderQuandoAcaoNaoExisteOuCotacaoEhIncompativel() {
        when(repository.findById(99L)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> service.atualizarCotacao(99L)).isInstanceOf(com.colavite.gestor_investimento.exception.AcaoNotFoundException.class);
        verify(selector, never()).para(any());

        Acao acao = new Acao("AAPL", "Apple Inc.", Mercado.ESTADOS_UNIDOS, new BigDecimal("200.00"), Instant.parse("2026-09-01T12:00:00Z"));
        when(repository.findById(3L)).thenReturn(java.util.Optional.of(acao));
        when(selector.para(Mercado.ESTADOS_UNIDOS)).thenReturn(usProvider);
        when(usProvider.consultarCotacao("AAPL")).thenReturn(new StockQuoteData("MSFT", Moeda.USD, new BigDecimal("213.49"), Instant.parse("2026-09-02T12:00:00Z")));
        assertThatThrownBy(() -> service.atualizarCotacao(3L)).isInstanceOf(com.colavite.gestor_investimento.exception.InvalidStockDataResponseException.class);
        verify(quoteUpdatePersistenceService, never()).atualizar(any(), any());
    }

    private StockRegistrationData data(String ticker, Moeda moeda) {
        return new StockRegistrationData(ticker, "Empresa", moeda, new BigDecimal("10.50"), Instant.parse("2026-09-01T12:00:00Z"));
    }

    private Acao acao(String ticker, Mercado mercado) {
        return new Acao(ticker, "Empresa", mercado, new BigDecimal("10.50"), Instant.parse("2026-09-01T12:00:00Z"));
    }
}
