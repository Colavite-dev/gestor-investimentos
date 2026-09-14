package com.colavite.gestor_investimento.controller;

import org.springframework.security.test.context.support.WithMockUser;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.exception.InvalidStockDataResponseException;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import com.colavite.gestor_investimento.exception.StockTickerNotFoundException;
import com.colavite.gestor_investimento.exception.StockVenueAmbiguityException;
import com.colavite.gestor_investimento.integration.stock.StockDataProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockCatalogProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockCatalogItemData;
import com.colavite.gestor_investimento.integration.stock.StockCatalogPageData;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import com.colavite.gestor_investimento.integration.stock.StockSuggestionData;
import com.colavite.gestor_investimento.integration.stock.brapi.BrapiStockAdapter;
import com.colavite.gestor_investimento.integration.stock.twelvedata.TwelveDataStockAdapter;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@WithMockUser
class AcaoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AcaoRepository repository;
    @MockitoBean StockDataProviderSelector selector;
    @MockitoBean StockCatalogProviderSelector catalogSelector;
    @MockitoBean BrapiStockAdapter brapiProvider;
    @MockitoBean TwelveDataStockAdapter twelveDataProvider;

    @BeforeEach
    void setUp() {
        when(selector.para(Mercado.BRASIL)).thenReturn(brapiProvider);
        when(selector.para(Mercado.ESTADOS_UNIDOS)).thenReturn(twelveDataProvider);
        when(catalogSelector.para(Mercado.BRASIL)).thenReturn(brapiProvider);
        when(catalogSelector.para(Mercado.ESTADOS_UNIDOS)).thenReturn(twelveDataProvider);
    }

    @Test
    void catalogoBrasileiroEhPaginadoENaoPersiste() throws Exception {
        when(brapiProvider.catalogar("pet", 1, 10)).thenReturn(new StockCatalogPageData(java.util.List.of(
                new StockCatalogItemData("PETR4", "Petrobras", Mercado.BRASIL, Moeda.BRL,
                        null, null, new BigDecimal("32.47"), "https://icons.brapi.dev/icons/PETR4.svg")
        ), 1, 10, true, 42L));

        mockMvc.perform(get("/acoes/catalogo").param("mercado", "BRASIL").param("q", "pet")
                        .param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$.items[0].logoUrl").value("https://icons.brapi.dev/icons/PETR4.svg"))
                .andExpect(jsonPath("$.items[0].cotacaoAtual").value(32.47))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalElements").value(42));

        assertThat(repository.count()).isZero();
    }

    @Test
    void catalogoValidaMercadoEPaginacao() throws Exception {
        mockMvc.perform(get("/acoes/catalogo").param("mercado", "INVALIDO"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/acoes/catalogo").param("mercado", "BRASIL").param("page", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/acoes/catalogo").param("mercado", "BRASIL").param("size", "51"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastraAcaoBrasileiraPeloNovoContrato() throws Exception {
        when(brapiProvider.consultar("PETR4")).thenReturn(data("PETR4", Moeda.BRL));

        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\" petr4 \",\"mercado\":\"BRASIL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.mercado").value("BRASIL"))
                .andExpect(jsonPath("$.moeda").value("BRL"));

        verify(brapiProvider).consultar("PETR4");
        verifyNoInteractions(twelveDataProvider);
    }

    @Test
    void cadastraAcaoAmericanaPeloProviderCorreto() throws Exception {
        when(twelveDataProvider.consultar("AAPL")).thenReturn(data("AAPL", Moeda.USD));

        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"aapl\",\"mercado\":\"ESTADOS_UNIDOS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.mercado").value("ESTADOS_UNIDOS"))
                .andExpect(jsonPath("$.moeda").value("USD"));

        verify(twelveDataProvider).consultar("AAPL");
        verifyNoInteractions(brapiProvider);
    }

    @Test
    void converteErrosSemConsultarProviderQuandoRequestInvalido() throws Exception {
        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"PETR4\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(selector, brapiProvider, twelveDataProvider);

        when(twelveDataProvider.consultar("ZERO")).thenThrow(new StockTickerNotFoundException());
        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"ZERO\",\"mercado\":\"ESTADOS_UNIDOS\"}"))
                .andExpect(status().isUnprocessableContent());

        when(twelveDataProvider.consultar("BAD")).thenThrow(new InvalidStockDataResponseException());
        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"BAD\",\"mercado\":\"ESTADOS_UNIDOS\"}"))
                .andExpect(status().isBadGateway());

        when(twelveDataProvider.consultar("DOWN")).thenThrow(new StockProviderUnavailableException());
        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"DOWN\",\"mercado\":\"ESTADOS_UNIDOS\"}"))
                .andExpect(status().isServiceUnavailable());

        when(twelveDataProvider.consultar("AMB")).thenThrow(new StockVenueAmbiguityException());
        mockMvc.perform(post("/acoes/resolver").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"AMB\",\"mercado\":\"ESTADOS_UNIDOS\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("múltiplos venues elegíveis")));
    }

    @Test
    void pesquisaAgregaBrasilEEstadosUnidos() throws Exception {
        when(brapiProvider.pesquisar("PET")).thenReturn(java.util.List.of(new StockSuggestionData("PETR4", "Petrobras", Mercado.BRASIL, Moeda.BRL)));
        when(twelveDataProvider.pesquisar("PET")).thenReturn(java.util.List.of(new StockSuggestionData("PET", "Pet Corp", Mercado.ESTADOS_UNIDOS, Moeda.USD)));

        mockMvc.perform(get("/acoes/pesquisar").param("q", " pet "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("PET"))
                .andExpect(jsonPath("$[1].ticker").value("PETR4"));

        verify(brapiProvider).pesquisar("PET");
        verify(twelveDataProvider).pesquisar("PET");
    }

    @Test
    void pesquisaToleraFalhaDeUmProvider() throws Exception {
        when(brapiProvider.pesquisar("AA")).thenThrow(new StockProviderUnavailableException());
        when(twelveDataProvider.pesquisar("AA")).thenReturn(java.util.List.of(new StockSuggestionData("AAPL", "Apple", Mercado.ESTADOS_UNIDOS, Moeda.USD)));

        mockMvc.perform(get("/acoes/pesquisar").param("q", "AA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"));
    }

    @Test
    void pesquisaClassificaIndisponibilidadeEConteudoInvalidoQuandoNenhumProviderResponde() throws Exception {
        when(brapiProvider.pesquisar("AA")).thenThrow(new StockProviderUnavailableException());
        when(twelveDataProvider.pesquisar("AA")).thenThrow(new StockProviderUnavailableException());
        mockMvc.perform(get("/acoes/pesquisar").param("q", "AA"))
                .andExpect(status().isServiceUnavailable());

        when(brapiProvider.pesquisar("BB")).thenThrow(new InvalidStockDataResponseException());
        when(twelveDataProvider.pesquisar("BB")).thenThrow(new InvalidStockDataResponseException());
        mockMvc.perform(get("/acoes/pesquisar").param("q", "BB"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void pesquisaRejeitaTermoCurtoAntesDosProviders() throws Exception {
        mockMvc.perform(get("/acoes/pesquisar").param("q", "A"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(selector, brapiProvider, twelveDataProvider);
    }

    @Test
    void converteDuplicidadePorTickerEMercado() throws Exception {
        when(twelveDataProvider.consultar("AAPL")).thenReturn(data("AAPL", Moeda.USD));
        String body = "{\"ticker\":\"AAPL\",\"mercado\":\"ESTADOS_UNIDOS\"}";

        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void atualizaCotacaoBrasileiraEPreservaDadosCadastrais() throws Exception {
        var acao = repository.saveAndFlush(new com.colavite.gestor_investimento.entity.Acao(
                "PETR4", "Petrobras", Mercado.BRASIL, new BigDecimal("20.00"), Instant.parse("2026-09-01T12:00:00Z")));
        when(brapiProvider.consultarCotacao("PETR4")).thenReturn(quote("PETR4", Moeda.BRL, "21.25"));

        mockMvc.perform(put("/acoes/{id}/atualizar-cotacao", acao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.nomeEmpresa").value("Petrobras"))
                .andExpect(jsonPath("$.mercado").value("BRASIL"))
                .andExpect(jsonPath("$.moeda").value("BRL"))
                .andExpect(jsonPath("$.cotacaoAtual").value(21.25))
                .andExpect(jsonPath("$.dataHoraCotacao").value("2026-09-02T12:00:00Z"));

        verify(brapiProvider).consultarCotacao("PETR4");
        verifyNoInteractions(twelveDataProvider);
    }

    @Test
    void atualizaCotacaoAmericanaPeloProviderCorreto() throws Exception {
        var acao = repository.saveAndFlush(new com.colavite.gestor_investimento.entity.Acao(
                "AAPL", "Apple Inc.", Mercado.ESTADOS_UNIDOS, new BigDecimal("200.00"), Instant.parse("2026-09-01T12:00:00Z")));
        when(twelveDataProvider.consultarCotacao("AAPL")).thenReturn(quote("AAPL", Moeda.USD, "213.49"));

        mockMvc.perform(put("/acoes/{id}/atualizar-cotacao", acao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.mercado").value("ESTADOS_UNIDOS"))
                .andExpect(jsonPath("$.moeda").value("USD"))
                .andExpect(jsonPath("$.cotacaoAtual").value(213.49));

        verify(twelveDataProvider).consultarCotacao("AAPL");
        verifyNoInteractions(brapiProvider);
    }

    @Test
    void converteErrosDaAtualizacaoSemAlterarAcao() throws Exception {
        mockMvc.perform(put("/acoes/{id}/atualizar-cotacao", 999L))
                .andExpect(status().isNotFound());
        verifyNoInteractions(selector, brapiProvider, twelveDataProvider);

        var acao = repository.saveAndFlush(new com.colavite.gestor_investimento.entity.Acao(
                "AAPL", "Apple Inc.", Mercado.ESTADOS_UNIDOS, new BigDecimal("200.00"), Instant.parse("2026-09-01T12:00:00Z")));
        doThrow(new StockTickerNotFoundException(), new InvalidStockDataResponseException(), new StockProviderUnavailableException())
                .when(twelveDataProvider).consultarCotacao("AAPL");
        mockMvc.perform(put("/acoes/{id}/atualizar-cotacao", acao.getId()))
                .andExpect(status().isUnprocessableContent());

        mockMvc.perform(put("/acoes/{id}/atualizar-cotacao", acao.getId()))
                .andExpect(status().isBadGateway());

        mockMvc.perform(put("/acoes/{id}/atualizar-cotacao", acao.getId()))
                .andExpect(status().isServiceUnavailable());
    }

    private StockRegistrationData data(String ticker, Moeda moeda) {
        return new StockRegistrationData(ticker, "Empresa", moeda, new BigDecimal("30.50"), Instant.parse("2026-09-01T12:00:00Z"));
    }

    private StockQuoteData quote(String ticker, Moeda moeda, String price) {
        return new StockQuoteData(ticker, moeda, new BigDecimal(price), Instant.parse("2026-09-02T12:00:00Z"));
    }
}
