package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StockDataProviderSelectorTest {

    @Test
    void selecionaProviderCorretoParaCadaMercado() {
        StockDataProvider brazil = mock(StockDataProvider.class);
        StockDataProvider unitedStates = mock(StockDataProvider.class);
        when(brazil.mercado()).thenReturn(Mercado.BRASIL);
        when(unitedStates.mercado()).thenReturn(Mercado.ESTADOS_UNIDOS);
        StockDataProviderSelector selector = new StockDataProviderSelector(List.of(brazil, unitedStates));

        assertThat(selector.para(Mercado.BRASIL)).isSameAs(brazil);
        assertThat(selector.para(Mercado.ESTADOS_UNIDOS)).isSameAs(unitedStates);
    }

    @Test
    void sinalizaProviderAusenteComoIndisponibilidade() {
        StockDataProvider brazil = mock(StockDataProvider.class);
        when(brazil.mercado()).thenReturn(Mercado.BRASIL);
        StockDataProviderSelector selector = new StockDataProviderSelector(List.of(brazil));

        assertThatThrownBy(() -> selector.para(Mercado.ESTADOS_UNIDOS))
                .isInstanceOf(StockProviderUnavailableException.class);
    }
}
