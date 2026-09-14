package com.colavite.gestor_investimento.integration.stock.brapi;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BrapiStockCatalogTest {
    @Test
    void mapeiaBuscaPaginacaoCotacaoELogoHttps() {
        Fixture fixture = fixture();
        fixture.server.expect(requestTo("http://brapi.test/api/quote/list?type=stock&page=2&limit=3&search=pet"))
                .andRespond(withSuccess("""
                        {"stocks":[
                          {"stock":"PETR4","name":"Petrobras PN","type":"stock","close":32.47,"logo":"https://icons.brapi.dev/icons/PETR4.svg"},
                          {"stock":"PETR3","name":"Petrobras ON","type":"stock","close":31.25,"logo":"http://inseguro.example/PETR3.svg"},
                          {"stock":"PETR11","name":"Fundo","type":"fund","close":10,"logo":"https://icons.brapi.dev/icons/PETR11.svg"}
                        ],"currentPage":2,"totalPages":5,"itemsPerPage":3,"totalCount":13,"hasNextPage":true}
                        """, MediaType.APPLICATION_JSON));

        var result = fixture.adapter.catalogar(" pet ", 1, 3);

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.totalElements()).isEqualTo(13L);
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).ticker()).isEqualTo("PETR4");
        assertThat(result.items().get(0).mercado()).isEqualTo(Mercado.BRASIL);
        assertThat(result.items().get(0).moeda()).isEqualTo(Moeda.BRL);
        assertThat(result.items().get(0).cotacaoAtual()).isEqualByComparingTo("32.47");
        assertThat(result.items().get(0).logoUrl()).isEqualTo("https://icons.brapi.dev/icons/PETR4.svg");
        assertThat(result.items().get(1).logoUrl()).isNull();
        fixture.server.verify();
    }

    @Test
    void classificaQuotaDoCatalogoComoIndisponibilidade() {
        Fixture fixture = fixture();
        fixture.server.expect(requestTo("http://brapi.test/api/quote/list?type=stock&page=1&limit=20"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> fixture.adapter.catalogar("", 0, 20))
                .isInstanceOf(StockProviderUnavailableException.class);
        fixture.server.verify();
    }

    private Fixture fixture() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://brapi.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(new BrapiStockAdapter(builder.build()), server);
    }

    private record Fixture(BrapiStockAdapter adapter, MockRestServiceServer server) {}
}
