package com.colavite.gestor_investimento.integration.cnpj.brasilapi;

import com.colavite.gestor_investimento.exception.CnpjNotFoundException;
import com.colavite.gestor_investimento.exception.CnpjProviderUnavailableException;
import com.colavite.gestor_investimento.exception.InvalidCnpjResponseException;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BrasilApiCnpjAdapterTest {

    private static final String CNPJ = "19131243000197";

    private MockRestServiceServer server;
    private HttpServer httpServer;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.verify();
        }
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void deveConsultarEndpointOficialEMapearCamposSnakeCase() {
        BrasilApiCnpjAdapter adapter = adapterWithMockServer();
        server.expect(once(), requestTo("http://brasil.test/api/cnpj/v1/" + CNPJ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(validJson(), MediaType.APPLICATION_JSON));

        CnpjRegistrationData data = adapter.consultar(CNPJ);

        assertThat(data).isEqualTo(new CnpjRegistrationData(
                CNPJ,
                "OPEN KNOWLEDGE BRASIL",
                "REDE PELO CONHECIMENTO LIVRE",
                "contato@example.org",
                "1123851939",
                "01311902",
                "PAULISTA 37",
                "37",
                "ANDAR 4",
                "BELA VISTA",
                "SAO PAULO",
                "SP",
                "ATIVA"
        ));
    }

    @Test
    void deveIgnorarCamposExternosAdicionaisENormalizarOpcionaisVazios() {
        BrasilApiCnpjAdapter adapter = adapterWithMockServer();
        server.expect(once(), requestTo("http://brasil.test/api/cnpj/v1/" + CNPJ))
                .andRespond(withSuccess(validJson()
                        .replace("\"nome_fantasia\": \"REDE PELO CONHECIMENTO LIVRE\"", "\"nome_fantasia\": \"\"")
                        .replace("\"email\": \"contato@example.org\"", "\"email\": null")
                        .replace("\"ddd_telefone_1\": \"1123851939\"", "\"ddd_telefone_1\": \"\"")
                        .replace("\"complemento\": \"ANDAR 4\"", "\"complemento\": \" \"")
                        .replace("\"campo_novo\": \"ignorado\"", "\"campo_novo\": {\"qualquer\": true}"),
                        MediaType.APPLICATION_JSON));

        CnpjRegistrationData data = adapter.consultar(CNPJ);

        assertThat(data.nomeFantasia()).isNull();
        assertThat(data.email()).isNull();
        assertThat(data.telefone()).isNull();
        assertThat(data.complemento()).isNull();
    }

    @Test
    void deveRejeitarCnpjDivergente() {
        BrasilApiCnpjAdapter adapter = adapterWithMockServer();
        server.expect(once(), requestTo("http://brasil.test/api/cnpj/v1/" + CNPJ))
                .andRespond(withSuccess(validJson().replace(CNPJ, "11222333000181"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.consultar(CNPJ))
                .isInstanceOf(InvalidCnpjResponseException.class);
    }

    @Test
    void deveRejeitarCampoObrigatorioAusenteOuAcimaDoLimite() {
        BrasilApiCnpjAdapter adapter = adapterWithMockServer();
        server.expect(once(), requestTo("http://brasil.test/api/cnpj/v1/" + CNPJ))
                .andRespond(withSuccess(validJson().replace("\"bairro\": \"BELA VISTA\"", "\"bairro\": null"),
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.consultar(CNPJ))
                .isInstanceOf(InvalidCnpjResponseException.class);
    }

    @Test
    void deveClassificar404ComoCnpjNaoEncontrado() {
        BrasilApiCnpjAdapter adapter = adapterWithMockServer();
        server.expect(once(), requestTo("http://brasil.test/api/cnpj/v1/" + CNPJ))
                .andRespond(withStatus(HttpStatusCode.valueOf(404)));

        assertThatThrownBy(() -> adapter.consultar(CNPJ))
                .isInstanceOf(CnpjNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 429, 500, 503})
    void deveClassificarDemaisErrosHttpComoIndisponibilidade(int status) {
        BrasilApiCnpjAdapter adapter = adapterWithMockServer();
        server.expect(once(), requestTo("http://brasil.test/api/cnpj/v1/" + CNPJ))
                .andRespond(withStatus(HttpStatusCode.valueOf(status)));

        assertThatThrownBy(() -> adapter.consultar(CNPJ))
                .isInstanceOf(CnpjProviderUnavailableException.class);
    }

    @Test
    void deveClassificarCorpoIlegivelComoRespostaInvalida() {
        BrasilApiCnpjAdapter adapter = adapterWithMockServer();
        server.expect(once(), requestTo("http://brasil.test/api/cnpj/v1/" + CNPJ))
                .andRespond(withSuccess("{json-incompleto", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.consultar(CNPJ))
                .isInstanceOf(InvalidCnpjResponseException.class);
    }

    @Test
    void deveClassificarTimeoutComoIndisponibilidade() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        httpServer.createContext("/api/cnpj/v1/" + CNPJ, exchange -> {
            try {
                Thread.sleep(150);
                byte[] body = validJson().getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        httpServer.start();

        SimpleClientHttpRequestFactory factory = requestFactory(Duration.ofSeconds(1), Duration.ofMillis(20));
        RestClient client = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + httpServer.getAddress().getPort())
                .requestFactory(factory)
                .build();

        assertThatThrownBy(() -> new BrasilApiCnpjAdapter(client).consultar(CNPJ))
                .isInstanceOf(CnpjProviderUnavailableException.class);
    }

    @Test
    void deveClassificarFalhaDeConexaoComoIndisponibilidade() throws Exception {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }

        SimpleClientHttpRequestFactory factory = requestFactory(Duration.ofMillis(100), Duration.ofMillis(100));
        RestClient client = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + closedPort)
                .requestFactory(factory)
                .build();

        assertThatThrownBy(() -> new BrasilApiCnpjAdapter(client).consultar(CNPJ))
                .isInstanceOf(CnpjProviderUnavailableException.class);
    }

    private BrasilApiCnpjAdapter adapterWithMockServer() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://brasil.test");
        server = MockRestServiceServer.bindTo(builder).build();
        return new BrasilApiCnpjAdapter(builder.build());
    }

    private SimpleClientHttpRequestFactory requestFactory(Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    private String validJson() {
        return """
                {
                  "cnpj": "19131243000197",
                  "razao_social": "OPEN KNOWLEDGE BRASIL",
                  "nome_fantasia": "REDE PELO CONHECIMENTO LIVRE",
                  "email": "contato@example.org",
                  "ddd_telefone_1": "1123851939",
                  "cep": "01311902",
                  "logradouro": "PAULISTA 37",
                  "numero": "37",
                  "complemento": "ANDAR 4",
                  "bairro": "BELA VISTA",
                  "municipio": "SAO PAULO",
                  "uf": "SP",
                  "descricao_situacao_cadastral": "ATIVA",
                  "campo_novo": "ignorado"
                }
                """;
    }
}
