package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.entity.*;
import com.colavite.gestor_investimento.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarteiraPosicaoServiceTest {
    @Mock private CarteiraRepository carteiras;
    @Mock private OperacaoRepository operacoes;

    @Test
    void calculaComprasEVendasIntercaladasComPrecoMedioPonderado() {
        Carteira carteira = new Carteira("C", "C", null);
        Acao acao = acao();
        when(carteiras.existsById(1L)).thenReturn(true);
        when(operacoes.findByCarteiraIdOrderByDataOperacaoAscIdAsc(1L)).thenReturn(List.of(
                operacao(carteira, acao, TipoOperacao.COMPRA, "10", "10"),
                operacao(carteira, acao, TipoOperacao.COMPRA, "10", "14"),
                operacao(carteira, acao, TipoOperacao.VENDA, "5", "12"),
                operacao(carteira, acao, TipoOperacao.COMPRA, "5", "16")
        ));

        var posicao = new CarteiraPosicaoService(carteiras, operacoes).listarPosicoes(1L).get(0);
        assertThat(posicao.quantidade()).isEqualByComparingTo("20");
        assertThat(posicao.precoMedio()).isEqualByComparingTo("13");
        assertThat(posicao.valorInvestido()).isEqualByComparingTo("260");
        assertThat(posicao.patrimonioAtual()).isEqualByComparingTo("240");
    }

    @Test
    void preservaPrecoMedioPonderadoAposVendaERecalculaNaCompraPosterior() {
        Carteira carteira = new Carteira("C", "C", null);
        Acao acao = acao();
        List<Operacao> antesDaNovaCompra = List.of(
                operacao(carteira, acao, TipoOperacao.COMPRA, "10", "20"),
                operacao(carteira, acao, TipoOperacao.COMPRA, "5", "30"),
                operacao(carteira, acao, TipoOperacao.VENDA, "5", "25")
        );
        List<Operacao> depoisDaNovaCompra = List.of(
                antesDaNovaCompra.get(0),
                antesDaNovaCompra.get(1),
                antesDaNovaCompra.get(2),
                operacao(carteira, acao, TipoOperacao.COMPRA, "10", "40")
        );
        when(carteiras.existsById(1L)).thenReturn(true);
        when(operacoes.findByCarteiraIdOrderByDataOperacaoAscIdAsc(1L))
                .thenReturn(antesDaNovaCompra, depoisDaNovaCompra);

        var aposVenda = new CarteiraPosicaoService(carteiras, operacoes).listarPosicoes(1L).get(0);
        var aposNovaCompra = new CarteiraPosicaoService(carteiras, operacoes).listarPosicoes(1L).get(0);

        assertThat(aposVenda.quantidade()).isEqualByComparingTo("10");
        assertThat(aposVenda.precoMedio()).isCloseTo(new BigDecimal("23.33333333"), within(new BigDecimal("0.00000001")));
        assertThat(aposNovaCompra.quantidade()).isEqualByComparingTo("20");
        assertThat(aposNovaCompra.precoMedio()).isEqualByComparingTo("31.66666667");
    }

    @Test
    void naoRetornaAtivoComSaldoFinalZero() {
        Carteira carteira = new Carteira("C", "C", null);
        Acao acao = acao();
        when(carteiras.existsById(1L)).thenReturn(true);
        when(operacoes.findByCarteiraIdOrderByDataOperacaoAscIdAsc(1L)).thenReturn(List.of(
                operacao(carteira, acao, TipoOperacao.COMPRA, "10", "10"),
                operacao(carteira, acao, TipoOperacao.VENDA, "10", "12")
        ));
        assertThat(new CarteiraPosicaoService(carteiras, operacoes).listarPosicoes(1L)).isEmpty();
    }

    @Test
    void falhaExplicitamenteParaHistoricoPersistidoInvalido() {
        Carteira carteira = new Carteira("C", "C", null);
        Acao acao = acao();
        when(carteiras.existsById(1L)).thenReturn(true);
        when(operacoes.findByCarteiraIdOrderByDataOperacaoAscIdAsc(1L))
                .thenReturn(List.of(operacao(carteira, acao, TipoOperacao.VENDA, "1", "12")));
        assertThatThrownBy(() -> new CarteiraPosicaoService(carteiras, operacoes).listarPosicoes(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    private Acao acao() {
        return new Acao("PETR4", "Petrobras", Mercado.BRASIL, new BigDecimal("12"), Instant.now());
    }

    private Operacao operacao(Carteira carteira, Acao acao, TipoOperacao tipo, String quantidade, String preco) {
        return new Operacao(carteira, acao, tipo, new BigDecimal(quantidade), new BigDecimal(preco), Instant.now());
    }
}
