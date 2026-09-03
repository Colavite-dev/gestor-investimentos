package com.colavite.gestor_investimento.integration.stock.twelvedata;

import com.colavite.gestor_investimento.config.TwelveDataConfig;
import com.colavite.gestor_investimento.config.TwelveDataProperties;
import com.colavite.gestor_investimento.entity.Moeda;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "runTwelveDataRealIT", matches = "true")
class TwelveDataStockRealIT {

    @Test
    void consultaDadosReaisDaApple() {
        String apiKey = System.getenv("TWELVE_DATA_API_KEY");
        Assumptions.assumeTrue(StringUtils.hasText(apiKey), "TWELVE_DATA_API_KEY não configurada no ambiente do processo");

        TwelveDataProperties properties = new TwelveDataProperties(
                URI.create("https://api.twelvedata.com"), Duration.ofSeconds(3), Duration.ofSeconds(8), apiKey);
        TwelveDataStockAdapter adapter = new TwelveDataStockAdapter(
                new TwelveDataConfig().twelveDataRestClient(properties), properties);

        var data = adapter.consultar("AAPL");

        assertThat(data.ticker()).isEqualTo("AAPL");
        assertThat(data.nomeEmpresa()).isNotBlank();
        assertThat(data.moeda()).isEqualTo(Moeda.USD);
        assertThat(data.cotacaoAtual()).isPositive();
        assertThat(data.dataHoraCotacao()).isNotNull();
    }
}
