package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.AcaoRequest;
import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.integration.stock.StockDataProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockCatalogProviderSelector;
import com.colavite.gestor_investimento.integration.stock.StockQuoteData;
import com.colavite.gestor_investimento.integration.stock.StockRegistrationData;
import com.colavite.gestor_investimento.integration.stock.brapi.BrapiStockAdapter;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import com.colavite.gestor_investimento.repository.CotacaoHistoricaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class AcaoQuoteHistoryRollbackIntegrationTest {

    private static final Instant ANTES = Instant.parse("2026-09-01T12:00:00Z");

    @Autowired private AcaoService service;
    @Autowired private AcaoRepository acoes;
    @Autowired private CotacaoHistoricaRepository historicoRepository;
    @MockitoBean private StockDataProviderSelector selector;
    @MockitoBean private StockCatalogProviderSelector catalogSelector;
    @MockitoBean private BrapiStockAdapter provider;
    @MockitoBean private CotacaoHistoricaService historico;

    @Test
    void falhaDoHistoricoNoCadastroFazRollbackDaAcao() {
        when(selector.para(Mercado.BRASIL)).thenReturn(provider);
        when(provider.consultar("ROLLC1")).thenReturn(new StockRegistrationData(
                "ROLLC1", "Cadastro rollback", Moeda.BRL, new BigDecimal("20.0000"), ANTES));
        falharRegistroHistorico();

        assertThatThrownBy(() -> service.cadastrar(new AcaoRequest("ROLLC1", Mercado.BRASIL)))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(acoes.findByTickerAndMercado("ROLLC1", Mercado.BRASIL)).isEmpty();
    }

    @Test
    void falhaDoHistoricoNaAtualizacaoFazRollbackDaCotacaoAtual() {
        Acao acao = acoes.saveAndFlush(new Acao("ROLLU1", "Atualização rollback", Mercado.BRASIL, new BigDecimal("60.0000"), ANTES));
        when(selector.para(Mercado.BRASIL)).thenReturn(provider);
        when(provider.consultarCotacao("ROLLU1")).thenReturn(new StockQuoteData(
                "ROLLU1", Moeda.BRL, new BigDecimal("61.0000"), Instant.parse("2026-09-02T12:00:00Z")));
        falharRegistroHistorico();

        assertThatThrownBy(() -> service.atualizarCotacao(acao.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);

        Acao persistida = acoes.findById(acao.getId()).orElseThrow();
        assertThat(persistida.getCotacaoAtual()).isEqualByComparingTo("60.0000");
        assertThat(persistida.getDataHoraCotacao()).isEqualTo(ANTES);
        assertThat(historicoRepository.findByAcaoIdOrderByDataHoraCotacaoAscIdAsc(acao.getId())).isEmpty();
    }

    private void falharRegistroHistorico() {
        doThrow(new DataIntegrityViolationException("falha de persistência do histórico"))
                .when(historico).registrar(any(), any(), any());
    }
}
