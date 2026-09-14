package com.colavite.gestor_investimento.integration.stock.twelvedata;

import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TwelveDataStockCatalogTest {
    private static final String STOCKS_URI = "http://twelve.test/stocks?country=United%20States&type=Common%20Stock&format=JSON&show_plan=false";

    @Test
    void filtraPaginaEUsaCacheSemCotacaoOuLogo() {
        Fixture fixture = fixture(Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"), ZoneOffset.UTC));
        fixture.server.expect(requestTo(STOCKS_URI))
                .andRespond(withSuccess(catalogPayload(), MediaType.APPLICATION_JSON));

        var first = fixture.adapter.catalogar("", 0, 1);
        var second = fixture.adapter.catalogar("", 1, 1);

        assertThat(first.items()).hasSize(1);
        assertThat(first.items().get(0).ticker()).isEqualTo("AAPL");
        assertThat(first.items().get(0).mercado()).isEqualTo(Mercado.ESTADOS_UNIDOS);
        assertThat(first.items().get(0).moeda()).isEqualTo(Moeda.USD);
        assertThat(first.items().get(0).exchange()).isEqualTo("NASDAQ");
        assertThat(first.items().get(0).logoUrl()).isNull();
        assertThat(first.items().get(0).cotacaoAtual()).isNull();
        assertThat(first.hasNext()).isTrue();
        assertThat(second.items()).extracting(item -> item.ticker()).containsExactly("MSFT");
        assertThat(second.hasNext()).isFalse();
        fixture.server.verify();
    }

    @Test
    void rejeitaInstrumentosCompostosOuForaDosVenuesAceitosMasPreservaAcoesNormais() {
        Fixture fixture = fixture(Clock.systemUTC());
        fixture.server.expect(requestTo(STOCKS_URI)).andRespond(withSuccess(catalogPayload(), MediaType.APPLICATION_JSON));

        var page = fixture.adapter.catalogar("", 0, 20);

        assertThat(page.items()).extracting(item -> item.ticker()).containsExactly("AAPL", "MSFT");
        assertThat(page.items()).isNotEmpty();
        fixture.server.verify();
    }

    @Test
    void recarregaSomenteDepoisDoTtl() {
        MutableClock clock = new MutableClock(Instant.parse("2026-09-10T12:00:00Z"));
        Fixture fixture = fixture(clock);
        fixture.server.expect(requestTo(STOCKS_URI)).andRespond(withSuccess(catalogPayload(), MediaType.APPLICATION_JSON));
        fixture.server.expect(requestTo(STOCKS_URI)).andRespond(withSuccess(catalogPayload(), MediaType.APPLICATION_JSON));

        fixture.adapter.catalogar("apple", 0, 20);
        fixture.adapter.catalogar("apple", 0, 20);
        clock.advance(Duration.ofHours(25));
        fixture.adapter.catalogar("apple", 0, 20);

        fixture.server.verify();
    }

    @Test
    void cargaConcorrenteFazUmaUnicaRequisicaoDeInventario() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/stocks", exchange -> {
            requests.incrementAndGet();
            byte[] body = catalogPayload().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        var executor = Executors.newFixedThreadPool(6);
        try {
            var adapter = new TwelveDataStockAdapter(
                    RestClient.builder().baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build(),
                    "key", Clock.systemUTC(), Duration.ofHours(24));
            var futures = java.util.stream.IntStream.range(0, 6)
                    .mapToObj(index -> executor.submit(() -> adapter.catalogar("", 0, 20)))
                    .toList();
            for (var future : futures) assertThat(future.get(5, TimeUnit.SECONDS).items()).hasSize(2);
            assertThat(requests).hasValue(1);
        } finally {
            executor.shutdownNow();
            server.stop(0);
        }
    }

    @Test
    void classificaQuotaDoInventarioComoIndisponibilidade() {
        Fixture fixture = fixture(Clock.systemUTC());
        fixture.server.expect(requestTo(STOCKS_URI)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> fixture.adapter.catalogar("", 0, 20))
                .isInstanceOf(StockProviderUnavailableException.class);
        fixture.server.verify();
    }

    private Fixture fixture(Clock clock) {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://twelve.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(new TwelveDataStockAdapter(builder.build(), "key", clock, Duration.ofHours(24)), server);
    }

    private static String catalogPayload() {
        return """
                {"count":9,"data":[
                  {"symbol":"MSFT","name":"Microsoft","currency":"USD","exchange":"NASDAQ","mic_code":"XNAS","country":"United States","type":"Common Stock"},
                  {"symbol":"AAPL","name":"Apple","currency":"USD","exchange":"NASDAQ","mic_code":"XNAS","country":"United States","type":"Common Stock"},
                  {"symbol":"SHOP","name":"Shopify","currency":"USD","exchange":"NYSE","mic_code":"XNYS","country":"Canada","type":"Common Stock"},
                  {"symbol":"SPY","name":"SPDR","currency":"USD","exchange":"NYSE","mic_code":"ARCX","country":"United States","type":"ETF"},
                  {"symbol":"EURX","name":"Euro","currency":"EUR","exchange":"NYSE","mic_code":"XNYS","country":"United States","type":"Common Stock"},
                  {"symbol":"DUP","name":"Duplicate A","currency":"USD","exchange":"NYSE","mic_code":"XNYS","country":"United States","type":"Common Stock"},
                  {"symbol":"DUP","name":"Duplicate B","currency":"USD","exchange":"NASDAQ","mic_code":"XNAS","country":"United States","type":"Common Stock"},
                  {"symbol":"!OTC/FLZH","name":"Inadequate OTC composite","currency":"USD","exchange":"OTC","mic_code":"OTCM","country":"United States","type":"Common Stock"},
                  {"symbol":"ABC/W","name":"Inadequate composite","currency":"USD","exchange":"NASDAQ","mic_code":"XNAS","country":"United States","type":"Common Stock"}
                ],"status":"ok"}
                """;
    }

    private record Fixture(TwelveDataStockAdapter adapter, MockRestServiceServer server) {}

    private static final class MutableClock extends Clock {
        private Instant instant;
        private MutableClock(Instant instant) { this.instant = instant; }
        void advance(Duration duration) { instant = instant.plus(duration); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
