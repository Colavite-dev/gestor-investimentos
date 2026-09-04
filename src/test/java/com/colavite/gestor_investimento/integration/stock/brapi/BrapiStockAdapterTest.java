package com.colavite.gestor_investimento.integration.stock.brapi;

import com.colavite.gestor_investimento.exception.InvalidStockDataResponseException;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import com.colavite.gestor_investimento.exception.StockTickerNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.ServerSocket;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BrapiStockAdapterTest {
    @Test void aceitaSomenteIdentidadeConfirmada() {
        Fixture fixture = fixture();
        fixture.server.expect(requestTo("http://brapi.test/api/v2/stocks/quote?symbols=PETR4"))
                .andRespond(withSuccess(payload("PETR4", "PETR4", false, "BRL", "10.50", "2026-09-01T12:00:00Z"), MediaType.APPLICATION_JSON));
        assertThat(fixture.adapter.consultar(" petr4 ").ticker()).isEqualTo("PETR4");
        fixture.server.verify();
    }

    @Test void rejeitaTickerRetornadoOuSolicitadoDivergenteERenomeacao() {
        assertInvalidIdentity(payload("PETR4", "VALE3", false, "BRL", "10", "2026-09-01T12:00:00Z"));
        assertInvalidIdentity(payload("VALE3", "PETR4", false, "BRL", "10", "2026-09-01T12:00:00Z"));
        assertInvalidIdentity(payload("VVAR3", "BHIA3", true, "BRL", "10", "2026-09-01T12:00:00Z"));
    }

    @Test void consultaCotacaoDiretaNaoExigeNomeMasExigeIdentidade() {
        Fixture fixture = fixture();
        fixture.server.expect(anything()).andRespond(withSuccess(payload("PETR4", "PETR4", false, "BRL", "10.50", "2026-09-01T12:00:00Z").replace("\"shortName\":\"Empresa\",", ""), MediaType.APPLICATION_JSON));
        assertThat(fixture.adapter.consultarCotacao("PETR4").ticker()).isEqualTo("PETR4");
        fixture.server.verify();
    }

    @Test void classificaTickerInexistenteE4xxGenerico() {
        Fixture empty = fixture(); empty.server.expect(anything()).andRespond(withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> empty.adapter.consultar("ZERO3")).isInstanceOf(StockTickerNotFoundException.class); empty.server.verify();
        Fixture missing = fixture(); missing.server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> missing.adapter.consultar("ZERO3")).isInstanceOf(StockTickerNotFoundException.class); missing.server.verify();
        Fixture generic = fixture(); generic.server.expect(anything()).andRespond(withStatus(HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> generic.adapter.consultar("PETR4")).isInstanceOf(InvalidStockDataResponseException.class); generic.server.verify();
    }

    @Test void rejeitaMoedaPrecoEPayloadInvalidos() {
        assertInvalidIdentity(payload("PETR4", "PETR4", false, "USD", "10", "2026-09-01T12:00:00Z"));
        assertInvalidIdentity(payload("PETR4", "PETR4", false, "BRL", "0", "2026-09-01T12:00:00Z"));
    }

    @Test void classificaAutorizacaoQuotaEServidorComoIndisponibilidade() {
        for (HttpStatus status : new HttpStatus[]{HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN, HttpStatus.TOO_MANY_REQUESTS}) {
            Fixture fixture = fixture(); fixture.server.expect(anything()).andRespond(withStatus(status));
            assertThatThrownBy(() -> fixture.adapter.consultar("PETR4")).isInstanceOf(StockProviderUnavailableException.class); fixture.server.verify();
        }
        Fixture server = fixture(); server.server.expect(anything()).andRespond(withServerError());
        assertThatThrownBy(() -> server.adapter.consultar("PETR4")).isInstanceOf(StockProviderUnavailableException.class); server.server.verify();
    }

    @Test void classificaConexaoETimeoutComoIndisponibilidade() throws Exception {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(Duration.ofMillis(100)); factory.setReadTimeout(Duration.ofMillis(100));
        int port; try (ServerSocket socket = new ServerSocket(0)) { port = socket.getLocalPort(); }
        BrapiStockAdapter connection = new BrapiStockAdapter(RestClient.builder().baseUrl("http://127.0.0.1:" + port).requestFactory(factory).build());
        assertThatThrownBy(() -> connection.consultar("PETR4")).isInstanceOf(StockProviderUnavailableException.class);
        try (ServerSocket slow = new ServerSocket(0)) { Thread thread = new Thread(() -> { try { slow.accept(); Thread.sleep(500); } catch (Exception ignored) { } }); thread.start(); BrapiStockAdapter timeout = new BrapiStockAdapter(RestClient.builder().baseUrl("http://127.0.0.1:" + slow.getLocalPort()).requestFactory(factory).build()); assertThatThrownBy(() -> timeout.consultar("PETR4")).isInstanceOf(StockProviderUnavailableException.class); thread.join(1000); }
    }

    private void assertInvalidIdentity(String body) { Fixture fixture = fixture(); fixture.server.expect(anything()).andRespond(withSuccess(body, MediaType.APPLICATION_JSON)); assertThatThrownBy(() -> fixture.adapter.consultar("PETR4")).isInstanceOf(InvalidStockDataResponseException.class); fixture.server.verify(); }
    private Fixture fixture() { RestClient.Builder builder = RestClient.builder().baseUrl("http://brapi.test"); MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build(); return new Fixture(new BrapiStockAdapter(builder.build()), server); }
    private String payload(String requested, String symbol, boolean changed, String currency, String price, String time) { return "{\"results\":[{\"requestedSymbol\":\"%s\",\"symbol\":\"%s\",\"changed\":%s,\"data\":{\"shortName\":\"Empresa\",\"currency\":\"%s\",\"regularMarketPrice\":%s,\"regularMarketTime\":\"%s\"}}]}".formatted(requested, symbol, changed, currency, price, time); }
    private record Fixture(BrapiStockAdapter adapter, MockRestServiceServer server) { }
}
