package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.AcaoResponse;
import com.colavite.gestor_investimento.dto.PosicaoResponse;
import com.colavite.gestor_investimento.entity.Carteira;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.exception.CarteiraNotFoundException;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import com.colavite.gestor_investimento.repository.CarteiraRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarteiraQuoteRefreshServiceTest {
    private static final long CARTEIRA_ID = 7L;
    private static final long USUARIO_ID = 9L;
    @Mock private CarteiraRepository carteiras;
    @Mock private CarteiraPosicaoService posicoes;
    @Mock private AcaoService acoes;

    @Test
    void atualizaAtivosBrEUsUmaVezCadaEIsolaFalhaParcial() {
        when(carteiras.findByIdAndUsuarioId(CARTEIRA_ID, USUARIO_ID)).thenReturn(Optional.of(mock(Carteira.class)));
        when(posicoes.listarPosicoes(CARTEIRA_ID, USUARIO_ID)).thenReturn(List.of(
                posicao(1L, "PETR4", Mercado.BRASIL, "2"), posicao(2L, "AAPL", Mercado.ESTADOS_UNIDOS, "1"),
                posicao(1L, "PETR4", Mercado.BRASIL, "3")
        ));
        when(acoes.atualizarCotacao(1L)).thenReturn(new AcaoResponse(1L, "PETR4", "Petrobras", Mercado.BRASIL,
                Moeda.BRL, new BigDecimal("30"), Instant.parse("2026-09-13T12:00:00Z")));
        when(acoes.atualizarCotacao(2L)).thenThrow(new StockProviderUnavailableException());

        var result = service().atualizarCotacoes(CARTEIRA_ID, USUARIO_ID);

        assertThat(result.quantidadeAtualizada()).isEqualTo(1);
        assertThat(result.quantidadeComFalha()).isEqualTo(1);
        assertThat(result.tickersComFalha()).containsExactly("AAPL");
        verify(acoes, times(1)).atualizarCotacao(1L);
        verify(acoes, times(1)).atualizarCotacao(2L);
    }

    @Test
    void carteiraSemPosicoesAbertasNaoConsultaProvider() {
        when(carteiras.findByIdAndUsuarioId(CARTEIRA_ID, USUARIO_ID)).thenReturn(Optional.of(mock(Carteira.class)));
        when(posicoes.listarPosicoes(CARTEIRA_ID, USUARIO_ID)).thenReturn(List.of());

        var result = service().atualizarCotacoes(CARTEIRA_ID, USUARIO_ID);

        assertThat(result.quantidadeAtualizada()).isZero();
        assertThat(result.quantidadeComFalha()).isZero();
        assertThat(result.tickersComFalha()).isEmpty();
        verifyNoInteractions(acoes);
    }

    @Test
    void carteiraDeOutroUsuarioRetorna404AntesDeCalcularOuConsultarProvider() {
        when(carteiras.findByIdAndUsuarioId(CARTEIRA_ID, USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().atualizarCotacoes(CARTEIRA_ID, USUARIO_ID)).isInstanceOf(CarteiraNotFoundException.class);

        verifyNoInteractions(posicoes, acoes);
    }

    private CarteiraQuoteRefreshService service() { return new CarteiraQuoteRefreshService(carteiras, posicoes, acoes); }

    private PosicaoResponse posicao(long id, String ticker, Mercado mercado, String quantidade) {
        return new PosicaoResponse(id, ticker, mercado, mercado.moeda(), new BigDecimal(quantidade), BigDecimal.ONE,
                new BigDecimal(quantidade), BigDecimal.ONE, new BigDecimal(quantidade), BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
