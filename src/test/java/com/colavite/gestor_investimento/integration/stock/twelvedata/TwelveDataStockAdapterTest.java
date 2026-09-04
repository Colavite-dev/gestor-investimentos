package com.colavite.gestor_investimento.integration.stock.twelvedata;

import com.colavite.gestor_investimento.entity.Moeda;
import com.colavite.gestor_investimento.exception.InvalidStockDataResponseException;
import com.colavite.gestor_investimento.exception.StockProviderUnavailableException;
import com.colavite.gestor_investimento.exception.StockTickerNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.ServerSocket;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TwelveDataStockAdapterTest {

    @Test
    void consultaCotacaoDiretaSemExecutarSymbolSearch() {
        Fixture fixture = fixture("test-key");
        fixture.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "apikey test-key"))
                .andRespond(withSuccess(quote("AAPL", "Apple Inc.", "USD", "213.49", 1756684800L), MediaType.APPLICATION_JSON));

        var data = fixture.adapter.consultarCotacao(" aapl ");

        assertThat(data.ticker()).isEqualTo("AAPL");
        assertThat(data.moeda()).isEqualTo(Moeda.USD);
        assertThat(data.cotacaoAtual()).isPositive();
        fixture.server.verify();
    }

    @Test
    void classificaErrosDaCotacaoDireta() {
        Fixture missing = fixture("key");
        missing.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andRespond(withSuccess("{\"status\":\"error\",\"code\":404}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> missing.adapter.consultarCotacao("AAPL")).isInstanceOf(StockTickerNotFoundException.class);
        missing.server.verify();

        Fixture rate = fixture("key");
        rate.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThatThrownBy(() -> rate.adapter.consultarCotacao("AAPL")).isInstanceOf(StockProviderUnavailableException.class);
        rate.server.verify();

        Fixture invalid = fixture("key");
        invalid.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andRespond(withSuccess(quote("AAPL", "Apple Inc.", "EUR", "213.49", 1756684800L), MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> invalid.adapter.consultarCotacao("AAPL")).isInstanceOf(InvalidStockDataResponseException.class);
        invalid.server.verify();
    }

    @Test
    void resolveAcaoAmericanaComMatchExatoEAutenticacao() {
        Fixture fixture = fixture("test-key");
        fixture.server.expect(requestTo("http://twelve.test/symbol_search?symbol=AAPL"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "apikey test-key"))
                .andRespond(withSuccess(search("AAPL", "Common Stock", "United States", "USD"), MediaType.APPLICATION_JSON));
        fixture.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "apikey test-key"))
                .andRespond(withSuccess(quote("AAPL", "Apple Inc.", "USD", "213.49", 1756684800L), MediaType.APPLICATION_JSON));

        var data = fixture.adapter.consultar("aapl");

        assertThat(data.ticker()).isEqualTo("AAPL");
        assertThat(data.nomeEmpresa()).isEqualTo("Apple Inc.");
        assertThat(data.moeda()).isEqualTo(Moeda.USD);
        assertThat(data.cotacaoAtual()).isEqualByComparingTo(new BigDecimal("213.49"));
        assertThat(data.dataHoraCotacao()).isNotNull();
        fixture.server.verify();
    }

    @Test
    void selecionaMatchValidoMesmoQuandoNaoEhPrimeiroResultado() {
        Fixture fixture = fixture("key");
        String payload = "{\"data\":["
                + "{\"symbol\":\"AAPL\",\"instrument_type\":\"ETF\",\"country\":\"United States\",\"currency\":\"USD\"},"
                + "{\"symbol\":\"AAPL\",\"instrument_type\":\"Common Stock\",\"country\":\"United States\",\"currency\":\"USD\"}],\"status\":\"ok\"}";
        fixture.server.expect(requestTo("http://twelve.test/symbol_search?symbol=AAPL"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));
        fixture.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andRespond(withSuccess(quote("AAPL", "Apple Inc.", "USD", "213.49", 1756684800L), MediaType.APPLICATION_JSON));

        assertThat(fixture.adapter.consultar("AAPL").ticker()).isEqualTo("AAPL");
        fixture.server.verify();
    }

    @Test
    void rejeitaInstrumentoSemMatchElegivelSemConsultarQuote() {
        Fixture fixture = fixture("key");
        fixture.server.expect(requestTo("http://twelve.test/symbol_search?symbol=SPY"))
                .andRespond(withSuccess(search("SPY", "ETF", "United States", "USD"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> fixture.adapter.consultar("SPY"))
                .isInstanceOf(StockTickerNotFoundException.class);
        fixture.server.verify();
    }

    @Test
    void rejeitaPaisTipoOuMoedaIncompativeis() {
        assertInvalidSearch(search("AAPL", "Common Stock", "Canada", "USD"));
        assertInvalidSearch(search("AAPL", "ETF", "United States", "USD"));
        assertInvalidSearch(search("AAPL", "Common Stock", "United States", "CAD"));
    }

    @Test
    void rejeitaQuoteIncompativel() {
        assertInvalidQuote(quote("MSFT", "Apple Inc.", "USD", "213.49", 1756684800L));
        assertInvalidQuote(quote("AAPL", "", "USD", "213.49", 1756684800L));
        assertInvalidQuote(quote("AAPL", "Apple Inc.", "EUR", "213.49", 1756684800L));
        assertInvalidQuote(quote("AAPL", "Apple Inc.", "USD", "0", 1756684800L));
        assertInvalidQuote(quote("AAPL", "Apple Inc.", "USD", "213.49", null));
    }

    @Test
    void classificaErrosHttpEStruturadosComoIndisponibilidade() {
        Fixture rate = fixture("key");
        rate.server.expect(requestTo("http://twelve.test/symbol_search?symbol=AAPL"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThatThrownBy(() -> rate.adapter.consultar("AAPL")).isInstanceOf(StockProviderUnavailableException.class);
        rate.server.verify();

        Fixture serverError = fixture("key");
        serverError.server.expect(requestTo("http://twelve.test/symbol_search?symbol=AAPL")).andRespond(withServerError());
        assertThatThrownBy(() -> serverError.adapter.consultar("AAPL")).isInstanceOf(StockProviderUnavailableException.class);
        serverError.server.verify();

        Fixture structured = fixture("key");
        structured.server.expect(requestTo("http://twelve.test/symbol_search?symbol=AAPL"))
                .andRespond(withSuccess("{\"status\":\"error\",\"code\":429}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> structured.adapter.consultar("AAPL")).isInstanceOf(StockProviderUnavailableException.class);
        structured.server.verify();

        Fixture unauthorized = fixture("key");
        unauthorized.server.expect(requestTo("http://twelve.test/symbol_search?symbol=AAPL"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        assertThatThrownBy(() -> unauthorized.adapter.consultar("AAPL")).isInstanceOf(StockProviderUnavailableException.class);
        unauthorized.server.verify();
    }

    @Test
    void classificaConexaoETimeoutComoIndisponibilidade() throws Exception {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(100));
        factory.setReadTimeout(Duration.ofMillis(100));

        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        TwelveDataStockAdapter connection = new TwelveDataStockAdapter(
                RestClient.builder().baseUrl("http://127.0.0.1:" + closedPort).requestFactory(factory).build(), "key");
        assertThatThrownBy(() -> connection.consultar("AAPL")).isInstanceOf(StockProviderUnavailableException.class);

        try (ServerSocket slowServer = new ServerSocket(0)) {
            Thread thread = new Thread(() -> {
                try {
                    slowServer.accept();
                    Thread.sleep(500);
                } catch (Exception ignored) {
                }
            });
            thread.start();
            TwelveDataStockAdapter timeout = new TwelveDataStockAdapter(
                    RestClient.builder().baseUrl("http://127.0.0.1:" + slowServer.getLocalPort()).requestFactory(factory).build(), "key");
            assertThatThrownBy(() -> timeout.consultar("AAPL")).isInstanceOf(StockProviderUnavailableException.class);
            thread.join(1000);
        }
    }

    @Test
    void trataApiKeyAusenteComoIndisponibilidadeSemChamada() {
        Fixture fixture = fixture(" ");

        assertThatThrownBy(() -> fixture.adapter.consultar("AAPL"))
                .isInstanceOf(StockProviderUnavailableException.class);
        fixture.server.verify();
    }

    @Test
    void classificaErroDeTickerIgualmenteEmEnvelopeEHttp400() {
        Fixture envelope = fixture("key");
        envelope.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andRespond(withSuccess(error(400, "Invalid symbol"), MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> envelope.adapter.consultarCotacao("AAPL")).isInstanceOf(StockTickerNotFoundException.class);
        envelope.server.verify();

        Fixture http = fixture("key");
        http.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(error(400, "Invalid symbol")));
        assertThatThrownBy(() -> http.adapter.consultarCotacao("AAPL")).isInstanceOf(StockTickerNotFoundException.class);
        http.server.verify();
    }

    @Test
    void rejeitaErro400GenericoComoRespostaExternaInvalida() {
        Fixture fixture = fixture("key");
        fixture.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(error(400, "Invalid parameter")));

        assertThatThrownBy(() -> fixture.adapter.consultarCotacao("AAPL"))
                .isInstanceOf(InvalidStockDataResponseException.class);
        fixture.server.verify();
    }

    @Test
    void rejeitaSimboloAmbiguoSemConsultarQuote() {
        Fixture fixture = fixture("key");
        String payload = "{\"data\":["
                + "{\"symbol\":\"AAPL\",\"instrument_type\":\"Common Stock\",\"country\":\"United States\",\"currency\":\"USD\"},"
                + "{\"symbol\":\"AAPL\",\"instrument_type\":\"Common Stock\",\"country\":\"United States\",\"currency\":\"USD\"}],\"status\":\"ok\"}";
        fixture.server.expect(requestTo("http://twelve.test/symbol_search?symbol=AAPL"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> fixture.adapter.consultar("AAPL"))
                .isInstanceOf(StockTickerNotFoundException.class);
        fixture.server.verify();
    }

    @Test
    void classificaForbiddenComoIndisponibilidade() {
        Fixture fixture = fixture("key");
        fixture.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));
        assertThatThrownBy(() -> fixture.adapter.consultarCotacao("AAPL"))
                .isInstanceOf(StockProviderUnavailableException.class);
        fixture.server.verify();
    }

    private void assertInvalidSearch(String searchPayload) {
        Fixture fixture = fixture("key");
        fixture.server.expect(requestTo("http://twelve.test/symbol_search?symbol=AAPL"))
                .andRespond(withSuccess(searchPayload, MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> fixture.adapter.consultar("AAPL")).isInstanceOf(StockTickerNotFoundException.class);
        fixture.server.verify();
    }

    private void assertInvalidQuote(String quotePayload) {
        Fixture fixture = fixture("key");
        fixture.server.expect(requestTo("http://twelve.test/symbol_search?symbol=AAPL"))
                .andRespond(withSuccess(search("AAPL", "Common Stock", "United States", "USD"), MediaType.APPLICATION_JSON));
        fixture.server.expect(requestTo("http://twelve.test/quote?symbol=AAPL"))
                .andRespond(withSuccess(quotePayload, MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> fixture.adapter.consultar("AAPL")).isInstanceOf(InvalidStockDataResponseException.class);
        fixture.server.verify();
    }

    private Fixture fixture(String apiKey) {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://twelve.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(new TwelveDataStockAdapter(builder.build(), apiKey), server);
    }

    private String search(String symbol, String type, String country, String currency) {
        return "{\"data\":[{\"symbol\":\"%s\",\"instrument_name\":\"Empresa\",\"instrument_type\":\"%s\",\"country\":\"%s\",\"currency\":\"%s\"}],\"status\":\"ok\"}"
                .formatted(symbol, type, country, currency);
    }

    private String quote(String symbol, String name, String currency, String close, Long timestamp) {
        String timestampValue = timestamp == null ? "null" : timestamp.toString();
        return "{\"symbol\":\"%s\",\"name\":\"%s\",\"currency\":\"%s\",\"close\":\"%s\",\"timestamp\":%s}"
                .formatted(symbol, name, currency, close, timestampValue);
    }

    private String error(int code, String message) {
        return "{\"status\":\"error\",\"code\":%d,\"message\":\"%s\"}".formatted(code, message);
    }

    private record Fixture(TwelveDataStockAdapter adapter, MockRestServiceServer server) {
    }
}
