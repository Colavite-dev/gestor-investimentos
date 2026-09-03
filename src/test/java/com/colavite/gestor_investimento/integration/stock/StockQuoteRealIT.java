package com.colavite.gestor_investimento.integration.stock;

import com.colavite.gestor_investimento.config.BrapiConfig;
import com.colavite.gestor_investimento.config.BrapiProperties;
import com.colavite.gestor_investimento.config.TwelveDataConfig;
import com.colavite.gestor_investimento.config.TwelveDataProperties;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.integration.stock.brapi.BrapiStockAdapter;
import com.colavite.gestor_investimento.integration.stock.twelvedata.TwelveDataStockAdapter;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "runStockQuoteRealIT", matches = "true")
class StockQuoteRealIT {

    @Test
    void consultaCotacaoDiretaDePetr4() {
        BrapiProperties properties = new BrapiProperties(
                URI.create("https://brapi.dev"), Duration.ofSeconds(3), Duration.ofSeconds(8), "");
        var quote = new BrapiStockAdapter(new BrapiConfig().brapiRestClient(properties)).consultarCotacao("PETR4");

        assertThat(quote.ticker()).isEqualTo("PETR4");
        assertThat(quote.moeda()).isEqualTo(Moeda.BRL);
        assertThat(quote.cotacaoAtual()).isPositive();
        assertThat(quote.dataHoraCotacao()).isNotNull();
    }

    @Test
    void consultaCotacaoDiretaDaApple() {
        String apiKey = System.getenv("TWELVE_DATA_API_KEY");
        Assumptions.assumeTrue(StringUtils.hasText(apiKey), "TWELVE_DATA_API_KEY não configurada no ambiente do processo");
        TwelveDataProperties properties = new TwelveDataProperties(
                URI.create("https://api.twelvedata.com"), Duration.ofSeconds(3), Duration.ofSeconds(8), apiKey);
        var quote = new TwelveDataStockAdapter(new TwelveDataConfig().twelveDataRestClient(properties), properties).consultarCotacao("AAPL");

        assertThat(quote.ticker()).isEqualTo("AAPL");
        assertThat(quote.moeda()).isEqualTo(Moeda.USD);
        assertThat(quote.cotacaoAtual()).isPositive();
        assertThat(quote.dataHoraCotacao()).isNotNull();
    }
}
