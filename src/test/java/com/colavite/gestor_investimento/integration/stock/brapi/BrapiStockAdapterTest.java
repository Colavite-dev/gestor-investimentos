package com.colavite.gestor_investimento.integration.stock.brapi;

import com.colavite.gestor_investimento.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.net.ServerSocket;
import java.time.Duration;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BrapiStockAdapterTest {
 @Test void consultaCotacaoDiretaSemExigirNomeDaEmpresa(){ Fixture f=fixture(); f.server.expect(requestTo("http://brapi.test/api/v2/stocks/quote?symbols=PETR4")).andRespond(withSuccess(payload("PETR4","PETR4","BRL","10.50","2026-09-01T12:00:00Z").replace("\"shortName\":\"Empresa\",", ""),MediaType.APPLICATION_JSON)); var data=f.adapter.consultarCotacao("PETR4"); assertThat(data.ticker()).isEqualTo("PETR4"); assertThat(data.cotacaoAtual()).isPositive(); f.server.verify(); }
 @Test void classificaErrosDaCotacaoDireta(){ Fixture missing=fixture(); missing.server.expect(anything()).andRespond(withSuccess("{\"results\":[]}",MediaType.APPLICATION_JSON)); assertThatThrownBy(()->missing.adapter.consultarCotacao("ZERO3")).isInstanceOf(StockTickerNotFoundException.class); missing.server.verify(); Fixture rate=fixture(); rate.server.expect(anything()).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)); assertThatThrownBy(()->rate.adapter.consultarCotacao("PETR4")).isInstanceOf(StockProviderUnavailableException.class); rate.server.verify(); }
 @Test void mapeiaQuoteEUsaTickerCanonico(){ Fixture f=fixture(); f.server.expect(requestTo("http://brapi.test/api/v2/stocks/quote?symbols=VVAR3")).andRespond(withSuccess(payload("VVAR3","BHIA3","BRL","10.50","2026-09-01T12:00:00Z"),MediaType.APPLICATION_JSON)); var data=f.adapter.consultar("VVAR3"); assertThat(data.ticker()).isEqualTo("BHIA3"); f.server.verify(); }
 @Test void classificaAusenciaEConteudoInvalido(){ Fixture absent=fixture(); absent.server.expect(anything()).andRespond(withSuccess("{\"results\":[]}",MediaType.APPLICATION_JSON)); assertThatThrownBy(()->absent.adapter.consultar("ZERO3")).isInstanceOf(StockTickerNotFoundException.class); absent.server.verify(); Fixture invalid=fixture(); invalid.server.expect(anything()).andRespond(withSuccess(payload("PETR4","PETR4","USD","10","2026-09-01T12:00:00Z"),MediaType.APPLICATION_JSON)); assertThatThrownBy(()->invalid.adapter.consultar("PETR4")).isInstanceOf(InvalidStockDataResponseException.class); invalid.server.verify(); }
 @Test void classificaHttpComoIndisponibilidade(){ Fixture rate=fixture(); rate.server.expect(anything()).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)); assertThatThrownBy(()->rate.adapter.consultar("PETR4")).isInstanceOf(StockProviderUnavailableException.class); rate.server.verify(); Fixture serverError=fixture(); serverError.server.expect(anything()).andRespond(withServerError()); assertThatThrownBy(()->serverError.adapter.consultar("PETR4")).isInstanceOf(StockProviderUnavailableException.class); serverError.server.verify(); }
 @Test void classificaConexaoETimeoutComoIndisponibilidade() throws Exception { int port; try(ServerSocket socket=new ServerSocket(0)){port=socket.getLocalPort();} SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();factory.setConnectTimeout(Duration.ofMillis(100));factory.setReadTimeout(Duration.ofMillis(100)); BrapiStockAdapter connection=new BrapiStockAdapter(RestClient.builder().baseUrl("http://127.0.0.1:"+port).requestFactory(factory).build());assertThatThrownBy(()->connection.consultar("PETR4")).isInstanceOf(StockProviderUnavailableException.class); try(ServerSocket slow=new ServerSocket(0)){Thread t=new Thread(()->{try{slow.accept();Thread.sleep(500);}catch(Exception ignored){}});t.start();BrapiStockAdapter timeout=new BrapiStockAdapter(RestClient.builder().baseUrl("http://127.0.0.1:"+slow.getLocalPort()).requestFactory(factory).build());assertThatThrownBy(()->timeout.consultar("PETR4")).isInstanceOf(StockProviderUnavailableException.class);t.join(1000);}}
 private Fixture fixture(){RestClient.Builder b=RestClient.builder().baseUrl("http://brapi.test"); MockRestServiceServer server=MockRestServiceServer.bindTo(b).build(); return new Fixture(new BrapiStockAdapter(b.build()),server);}
 private String payload(String requested,String symbol,String currency,String price,String time){return "{\"results\":[{\"requestedSymbol\":\"%s\",\"symbol\":\"%s\",\"changed\":true,\"data\":{\"shortName\":\"Empresa\",\"currency\":\"%s\",\"regularMarketPrice\":%s,\"regularMarketTime\":\"%s\"}}]}".formatted(requested,symbol,currency,price,time);}
 private record Fixture(BrapiStockAdapter adapter, MockRestServiceServer server){}
}
