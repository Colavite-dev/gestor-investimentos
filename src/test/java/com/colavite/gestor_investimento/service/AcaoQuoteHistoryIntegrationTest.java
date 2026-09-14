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
import com.colavite.gestor_investimento.repository.CarteiraRepository;
import com.colavite.gestor_investimento.repository.OperacaoRepository;
import com.colavite.gestor_investimento.entity.Carteira;
import com.colavite.gestor_investimento.entity.Operacao;
import com.colavite.gestor_investimento.entity.TipoOperacao;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import com.colavite.gestor_investimento.support.TestUsuarios;
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
    @Autowired private CotacaoHistoricaService historicoService;
    @Autowired private AcaoRepository acoes;
    @Autowired private CotacaoHistoricaRepository historico;
    @Autowired private CarteiraRepository carteiras;
    @Autowired private OperacaoRepository operacoes;
    @Autowired private CarteiraPosicaoService posicoes;
    @Autowired private UsuarioRepository usuarios;
    @MockitoBean private StockDataProviderSelector selector;
    @MockitoBean private StockCatalogProviderSelector catalogSelector;
    @MockitoBean private BrapiStockAdapter provider;

    @Test
    void cadastroPersisteAcaoEHistoricoInicialComMesmaCotacaoETimestamp() {
        when(selector.para(Mercado.BRASIL)).thenReturn(provider);
        when(provider.consultar("PETR4")).thenReturn(new StockRegistrationData(
                "PETR4", "Petrobras", Moeda.BRL, new BigDecimal("20.12345678"), INICIAL));

        service.cadastrar(new AcaoRequest("PETR4", Mercado.BRASIL));

        Acao acao = acoes.findByTickerAndMercado("PETR4", Mercado.BRASIL).orElseThrow();
        var observacao = historico.findByAcaoIdOrderByDataHoraCotacaoAscIdAsc(acao.getId()).get(0);
        assertThat(observacao.getCotacao()).isEqualByComparingTo(acao.getCotacaoAtual());
        assertThat(acao.getCotacaoAtual()).isEqualByComparingTo("20.12345678");
        assertThat(observacao.getCotacao()).isEqualByComparingTo("20.12345678");
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

    @Test
    void primeiraObservacaoEExataDuplicataNaoCriamDuasLinhas() {
        Acao acao = acoes.saveAndFlush(new Acao("DUPL1", "Duplicata", Mercado.BRASIL, new BigDecimal("10"), INICIAL));
        historicoService.registrar(acao, new BigDecimal("10.00"), INICIAL);
        historicoService.registrar(acao, new BigDecimal("10.00"), INICIAL);

        assertThat(historico.findByAcaoIdOrderByDataHoraCotacaoAscIdAsc(acao.getId())).hasSize(1);
    }

    @Test
    void mesmaCotacaoComTimestampDiferenteEGravada() {
        Acao acao = acoes.saveAndFlush(new Acao("DUPL2", "Duplicata", Mercado.BRASIL, new BigDecimal("10"), INICIAL));
        historicoService.registrar(acao, new BigDecimal("10.00"), INICIAL);
        historicoService.registrar(acao, new BigDecimal("10.00"), ATUALIZADA);

        assertThat(historico.findByAcaoIdOrderByDataHoraCotacaoAscIdAsc(acao.getId())).hasSize(2);
    }

    @Test
    void cotacaoDiferenteEGravadaMesmoComFluxoDeHistoricoExistente() {
        Acao acao = acoes.saveAndFlush(new Acao("DUPL3", "Duplicata", Mercado.BRASIL, new BigDecimal("10"), INICIAL));
        historicoService.registrar(acao, new BigDecimal("10.00"), INICIAL);
        historicoService.registrar(acao, new BigDecimal("11.00"), INICIAL);

        assertThat(historico.findByAcaoIdOrderByDataHoraCotacaoAscIdAsc(acao.getId())).hasSize(2);
    }

    @Test
    void atualizarCotacaoAlteraValuationSemReescreverPrecoExecutadoOuCusto() {
        var owner = TestUsuarios.persistir(usuarios, "quote-history");
        Carteira carteira = carteiras.saveAndFlush(new Carteira("Separação", "SEPARAÇÃO", null, owner));
        Acao acao = acoes.saveAndFlush(new Acao("ITUB4", "Itaú", Mercado.BRASIL, new BigDecimal("32"), INICIAL));
        Operacao operacao = operacoes.saveAndFlush(new Operacao(carteira, acao, TipoOperacao.COMPRA,
                new BigDecimal("10"), new BigDecimal("30"), INICIAL));
        when(selector.para(Mercado.BRASIL)).thenReturn(provider);
        when(provider.consultarCotacao("ITUB4")).thenReturn(new StockQuoteData(
                "ITUB4", Moeda.BRL, new BigDecimal("35"), ATUALIZADA));

        service.atualizarCotacao(acao.getId());

        Operacao persistida = operacoes.findById(operacao.getId()).orElseThrow();
        var posicao = posicoes.listarPosicoes(carteira.getId(), owner.getId()).get(0);
        assertThat(persistida.getPrecoUnitario()).isEqualByComparingTo("30");
        assertThat(posicao.precoMedio()).isEqualByComparingTo("30");
        assertThat(posicao.valorInvestido()).isEqualByComparingTo("300");
        assertThat(posicao.cotacaoAtual()).isEqualByComparingTo("35");
        assertThat(posicao.patrimonioAtual()).isEqualByComparingTo("350");
    }
}
