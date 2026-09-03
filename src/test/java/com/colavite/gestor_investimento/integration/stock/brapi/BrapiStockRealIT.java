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
 @Test void consultaQuoteRealDePetr4(){ var adapter=new BrapiStockAdapter(new BrapiConfig().brapiRestClient(new BrapiProperties(URI.create("https://brapi.dev"),Duration.ofSeconds(3),Duration.ofSeconds(8),""))); var data=adapter.consultar("PETR4"); assertThat(data.ticker()).isEqualTo("PETR4"); assertThat(data.nomeEmpresa()).isNotBlank(); assertThat(data.cotacaoAtual()).isPositive(); assertThat(data.dataHoraCotacao()).isNotNull(); }
}
