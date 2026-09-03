package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.OperacaoRequest;
import com.colavite.gestor_investimento.entity.*;
import com.colavite.gestor_investimento.exception.SaldoInsuficienteParaVendaException;
import com.colavite.gestor_investimento.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperacaoServiceTest {
    private static final Instant DIA_1 = Instant.parse("2026-09-01T12:00:00Z");
    private static final Instant DIA_2 = Instant.parse("2026-09-02T12:00:00Z");
    private static final Instant DIA_3 = Instant.parse("2026-09-03T12:00:00Z");

    @Mock private OperacaoRepository operacoes;
    @Mock private CarteiraRepository carteiras;
    @Mock private AcaoRepository acoes;
    private Carteira carteira;
    private Acao acao;
    private OperacaoService service;

    @BeforeEach
    void setUp() {
        carteira = new Carteira("Carteira", "CARTEIRA", null);
        acao = new Acao("PETR4", "Petrobras", Mercado.BRASIL, new BigDecimal("30"), DIA_1);
        service = new OperacaoService(operacoes, carteiras, acoes);
        when(carteiras.findByIdForUpdate(1L)).thenReturn(Optional.of(carteira));
        when(acoes.findById(2L)).thenReturn(Optional.of(acao));
        lenient().when(operacoes.saveAndFlush(any(Operacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void permiteVendaParcialQuandoHaSaldoSuficiente() {
        vendasExistentes(operacao(TipoOperacao.COMPRA, "10", DIA_1));
        var response = service.cadastrar(request(TipoOperacao.VENDA, "5", DIA_2));
        assertThat(response.tipo()).isEqualTo(TipoOperacao.VENDA);
        verify(operacoes).saveAndFlush(any(Operacao.class));
    }

    @Test
    void permiteVendaQueZeraSaldo() {
        vendasExistentes(operacao(TipoOperacao.COMPRA, "10", DIA_1));
        service.cadastrar(request(TipoOperacao.VENDA, "10", DIA_2));
        verify(operacoes).saveAndFlush(any(Operacao.class));
    }

    @Test
    void rejeitaVendaSuperiorAoSaldoSemPersistir() {
        vendasExistentes(operacao(TipoOperacao.COMPRA, "10", DIA_1));
        assertThatThrownBy(() -> service.cadastrar(request(TipoOperacao.VENDA, "11", DIA_2)))
                .isInstanceOf(SaldoInsuficienteParaVendaException.class);
        verify(operacoes, never()).saveAndFlush(any());
    }

    @Test
    void rejeitaVendaSemCompraAnteriorSemPersistir() {
        vendasExistentes();
        assertThatThrownBy(() -> service.cadastrar(request(TipoOperacao.VENDA, "1", DIA_1)))
                .isInstanceOf(SaldoInsuficienteParaVendaException.class);
        verify(operacoes, never()).saveAndFlush(any());
    }

    @Test
    void rejeitaVendaRetroativaQueInvalidaVendaPosterior() {
        vendasExistentes(operacao(TipoOperacao.COMPRA, "10", DIA_1), operacao(TipoOperacao.VENDA, "8", DIA_3));
        assertThatThrownBy(() -> service.cadastrar(request(TipoOperacao.VENDA, "5", DIA_2)))
                .isInstanceOf(SaldoInsuficienteParaVendaException.class);
        verify(operacoes, never()).saveAndFlush(any());
    }

    @Test
    void permiteVendaRetroativaQuandoTodaSequenciaPermaneceValida() {
        vendasExistentes(operacao(TipoOperacao.COMPRA, "10", DIA_1), operacao(TipoOperacao.VENDA, "8", DIA_3));
        service.cadastrar(request(TipoOperacao.VENDA, "2", DIA_2));
        verify(operacoes).saveAndFlush(any(Operacao.class));
    }

    @Test
    void consideraVendaComMesmoTimestampDepoisDasOperacoesPersistidas() {
        vendasExistentes(operacao(TipoOperacao.COMPRA, "10", DIA_1));
        service.cadastrar(request(TipoOperacao.VENDA, "10", DIA_1));
        verify(operacoes).saveAndFlush(any(Operacao.class));
    }

    @Test
    void cadastraCompraSemConsultarSequencia() {
        var response = service.cadastrar(request(TipoOperacao.COMPRA, "10", DIA_1));
        assertThat(response.tipo()).isEqualTo(TipoOperacao.COMPRA);
        verify(operacoes).saveAndFlush(any(Operacao.class));
        verify(operacoes, never()).findByCarteiraIdAndAcaoIdOrderByDataOperacaoAscIdAsc(any(), any());
    }

    private void vendasExistentes(Operacao... existentes) {
        when(operacoes.findByCarteiraIdAndAcaoIdOrderByDataOperacaoAscIdAsc(1L, 2L)).thenReturn(List.of(existentes));
    }

    private OperacaoRequest request(TipoOperacao tipo, String quantidade, Instant data) {
        return new OperacaoRequest(1L, 2L, tipo, new BigDecimal(quantidade), new BigDecimal("30"), data);
    }

    private Operacao operacao(TipoOperacao tipo, String quantidade, Instant data) {
        return new Operacao(carteira, acao, tipo, new BigDecimal(quantidade), new BigDecimal("30"), data);
    }
}
