package com.colavite.gestor_investimento.integration.stock.brapi;

import com.colavite.gestor_investimento.config.BrapiConfig;
import com.colavite.gestor_investimento.config.BrapiProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.net.URI;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "runBrapiRealIT", matches = "true")
class BrapiStockRealIT {
    private BrapiStockAdapter adapter() {
        String token = System.getenv("BRAPI_TOKEN");
        var properties = new BrapiProperties(
                URI.create("https://brapi.dev"), Duration.ofSeconds(3), Duration.ofSeconds(12), token == null ? "" : token);
        return new BrapiStockAdapter(new BrapiConfig().brapiRestClient(properties));
    }

    @Test
    void consultaQuoteRealDePetr4() {
        var data = adapter().consultar("PETR4");
        assertThat(data.ticker()).isEqualTo("PETR4");
        assertThat(data.nomeEmpresa()).isNotBlank();
        assertThat(data.cotacaoAtual()).isPositive();
        assertThat(data.dataHoraCotacao()).isNotNull();
    }

    @Test
    void consultaUmaPaginaRealDoCatalogoBrasileiro() {
        var page = adapter().catalogar("PETR", 0, 5);

        assertThat(page.items()).isNotEmpty().hasSizeLessThanOrEqualTo(5);
        assertThat(page.items()).allSatisfy(item -> {
            assertThat(item.ticker().contains("PETR") || item.nomeEmpresa().toUpperCase().contains("PETR")).isTrue();
            assertThat(item.nomeEmpresa()).isNotBlank();
            assertThat(item.mercado().name()).isEqualTo("BRASIL");
            assertThat(item.moeda().name()).isEqualTo("BRL");
            if (item.logoUrl() != null) assertThat(item.logoUrl()).startsWith("https://");
        });
    }
}
