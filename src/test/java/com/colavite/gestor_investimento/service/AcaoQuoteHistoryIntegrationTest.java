package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.AcaoRequest;
import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.integration.stock.StockDataProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import com.colavite.gestor_investimento.integration.stock.brapi.BrapiStockAdapter;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import com.colavite.gestor_investimento.repository.CotacaoHistoricaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class AcaoQuoteHistoryIntegrationTest {

    private static final Instant INICIAL = Instant.parse("2026-09-01T12:00:00Z");
    private static final Instant ATUALIZADA = Instant.parse("2026-09-02T12:00:00Z");

    @Autowired private AcaoService service;
    @Autowired private AcaoRepository acoes;
    @Autowired private CotacaoHistoricaRepository historico;
    @MockitoBean private StockDataProviderSelector selector;
    @MockitoBean private BrapiStockAdapter provider;

    @Test
    void cadastroPersisteAcaoEHistoricoInicialComMesmaCotacaoETimestamp() {
        when(selector.para(Mercado.BRASIL)).thenReturn(provider);
        when(provider.consultar("PETR4")).thenReturn(new StockRegistrationData(
                "PETR4", "Petrobras", Moeda.BRL, new BigDecimal("20.1234"), INICIAL));

        service.cadastrar(new AcaoRequest("PETR4", Mercado.BRASIL));

        Acao acao = acoes.findByTickerAndMercado("PETR4", Mercado.BRASIL).orElseThrow();
        var observacao = historico.findByAcaoIdOrderByDataHoraCotacaoAscIdAsc(acao.getId()).get(0);
        assertThat(observacao.getCotacao()).isEqualByComparingTo(acao.getCotacaoAtual());
        assertThat(observacao.getDataHoraCotacao()).isEqualTo(acao.getDataHoraCotacao());
        assertThat(observacao.getDataHoraCotacao()).isEqualTo(INICIAL);
    }

    @Test
    void atualizacaoPersisteCotacaoAtualEHistoricoComMesmaCotacaoETimestamp() {
        Acao acao = acoes.saveAndFlush(new Acao("VALE3", "Vale", Mercado.BRASIL, new BigDecimal("60.0000"), INICIAL));
        when(selector.para(Mercado.BRASIL)).thenReturn(provider);
        when(provider.consultarCotacao("VALE3")).thenReturn(new StockQuoteData(
                "VALE3", Moeda.BRL, new BigDecimal("61.4321"), ATUALIZADA));

        service.atualizarCotacao(acao.getId());

        Acao atualizada = acoes.findById(acao.getId()).orElseThrow();
        var observacao = historico.findByAcaoIdOrderByDataHoraCotacaoAscIdAsc(acao.getId()).get(0);
        assertThat(observacao.getCotacao()).isEqualByComparingTo(atualizada.getCotacaoAtual());
        assertThat(observacao.getDataHoraCotacao()).isEqualTo(atualizada.getDataHoraCotacao());
        assertThat(observacao.getDataHoraCotacao()).isEqualTo(ATUALIZADA);
    }
}
