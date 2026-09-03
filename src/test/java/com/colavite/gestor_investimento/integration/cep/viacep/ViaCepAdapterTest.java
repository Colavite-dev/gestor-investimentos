package com.colavite.gestor_investimento.integration.cep.viacep;

import com.colavite.gestor_investimento.exception.CepNotFoundException;
import com.colavite.gestor_investimento.exception.CepProviderUnavailableException;
import com.colavite.gestor_investimento.exception.InvalidCepResponseException;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.net.ServerSocket;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.http.HttpMethod.GET;

class ViaCepAdapterTest {
    private MockRestServiceServer server;

    @AfterEach void verifyServer() { if (server != null) server.verify(); }

    @Test void mapeiaRespostaECaminhoOficial() {
        ViaCepAdapter adapter = adapter();
        server.expect(requestTo("http://viacep.test/ws/01001000/json/")).andExpect(method(GET))
                .andRespond(withSuccess("{\"cep\":\"01001-000\",\"logradouro\":\"Praça da Sé\",\"bairro\":\"Sé\",\"localidade\":\"São Paulo\",\"uf\":\"SP\"}", MediaType.APPLICATION_JSON));
        CepAddressData result = adapter.consultar("01001000");
        assertThat(result).isEqualTo(new CepAddressData("01001000", "Praça da Sé", "Sé", "São Paulo", "SP"));
    }

    @Test void classificaCepNaoEncontrado() {
        ViaCepAdapter adapter = adapter();
        server.expect(requestTo("http://viacep.test/ws/99999999/json/")).andRespond(withSuccess("{\"erro\":true}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> adapter.consultar("99999999")).isInstanceOf(CepNotFoundException.class);
    }

    @Test void classificaRespostaInvalidaEErroHttp() {
        ViaCepAdapter invalid = adapter();
        server.expect(requestTo("http://viacep.test/ws/01001000/json/")).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> invalid.consultar("01001000")).isInstanceOf(InvalidCepResponseException.class);
        server = null;
        ViaCepAdapter unavailable = adapter();
        server.expect(requestTo("http://viacep.test/ws/01001000/json/")).andRespond(withServerError());
        assertThatThrownBy(() -> unavailable.consultar("01001000")).isInstanceOf(CepProviderUnavailableException.class);
    }

    @Test void classificaRateLimitEConexaoComoIndisponibilidade() throws Exception {
        ViaCepAdapter rateLimit = adapter();
        server.expect(requestTo("http://viacep.test/ws/01001000/json/")).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThatThrownBy(() -> rateLimit.consultar("01001000")).isInstanceOf(CepProviderUnavailableException.class);
        server = null;
        int port;
        try (ServerSocket socket = new ServerSocket(0)) { port = socket.getLocalPort(); }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(100));
        factory.setReadTimeout(Duration.ofMillis(100));
        ViaCepAdapter connection = new ViaCepAdapter(RestClient.builder().baseUrl("http://127.0.0.1:" + port).requestFactory(factory).build());
        assertThatThrownBy(() -> connection.consultar("01001000")).isInstanceOf(CepProviderUnavailableException.class);
        try (ServerSocket slowServer = new ServerSocket(0)) {
            Thread responder = new Thread(() -> { try { slowServer.accept(); Thread.sleep(500); } catch (Exception ignored) { } });
            responder.start();
            ViaCepAdapter timeout = new ViaCepAdapter(RestClient.builder().baseUrl("http://127.0.0.1:" + slowServer.getLocalPort()).requestFactory(factory).build());
            assertThatThrownBy(() -> timeout.consultar("01001000")).isInstanceOf(CepProviderUnavailableException.class);
            responder.join(1000);
        }
    }

    private ViaCepAdapter adapter() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://viacep.test");
        server = MockRestServiceServer.bindTo(builder).build();
        return new ViaCepAdapter(builder.build());
    }
}
