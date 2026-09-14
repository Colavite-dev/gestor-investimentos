package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StockCatalogProviderSelectorTest {
    @Test
    void selecionaCatalogoPeloMercado() {
        StockCatalogProvider br = mock(StockCatalogProvider.class);
        StockCatalogProvider us = mock(StockCatalogProvider.class);
        when(br.mercado()).thenReturn(Mercado.BRASIL);
        when(us.mercado()).thenReturn(Mercado.ESTADOS_UNIDOS);

        StockCatalogProviderSelector selector = new StockCatalogProviderSelector(List.of(br, us));

        assertThat(selector.para(Mercado.BRASIL)).isSameAs(br);
        assertThat(selector.para(Mercado.ESTADOS_UNIDOS)).isSameAs(us);
    }

    @Test
    void rejeitaProviderAusenteOuDuplicado() {
        StockCatalogProvider br1 = mock(StockCatalogProvider.class);
        StockCatalogProvider br2 = mock(StockCatalogProvider.class);
        when(br1.mercado()).thenReturn(Mercado.BRASIL);
        when(br2.mercado()).thenReturn(Mercado.BRASIL);

        assertThatThrownBy(() -> new StockCatalogProviderSelector(List.of(br1, br2)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new StockCatalogProviderSelector(List.of()).para(Mercado.BRASIL))
                .isInstanceOf(StockProviderUnavailableException.class);
    }
}
