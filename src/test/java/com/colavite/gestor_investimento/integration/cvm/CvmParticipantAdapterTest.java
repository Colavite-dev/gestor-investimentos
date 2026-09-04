package com.colavite.gestor_investimento.integration.cvm;

import com.colavite.gestor_investimento.config.CvmProperties;
import com.colavite.gestor_investimento.exception.CvmProviderUnavailableException;
import com.colavite.gestor_investimento.exception.InvalidCvmResponseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CvmParticipantAdapterTest {

    private MockRestServiceServer server;

    @AfterEach
    void verifyServer() {
        if (server != null) server.verify();
    }

    @Test
    void localizaParticipantePorCnpjNormalizadoELeSomenteCadastroBasico() throws Exception {
        MutableClock clock = new MutableClock();
        CvmParticipantAdapter adapter = adapter(clock);
        server.expect(requestTo("http://cvm.test/cad_intermed.zip")).andExpect(method(GET))
                .andRespond(withSuccess(zip("CNPJ;SIT;TP_REGISTRO\n11.222.333/0001-81;ATIVO;CORRETORA DE TITULOS E VALORES MOBILIARIOS\n", true), MediaType.APPLICATION_OCTET_STREAM));

        Optional<CvmParticipantData> participant = adapter.consultar("11.222.333/0001-81");

        assertThat(participant).contains(new CvmParticipantData("11222333000181", "ATIVO", "CORRETORA DE TITULOS E VALORES MOBILIARIOS"));
    }

    @Test
    void rejeitaZipOuCabecalhosIncompativeis() throws Exception {
        CvmParticipantAdapter invalidZip = adapter(new MutableClock());
        server.expect(requestTo("http://cvm.test/cad_intermed.zip")).andRespond(withSuccess("not-a-zip", MediaType.APPLICATION_OCTET_STREAM));
        assertThatThrownBy(() -> invalidZip.consultar("11222333000181")).isInstanceOf(InvalidCvmResponseException.class);

        server = null;
        CvmParticipantAdapter invalidHeaders = adapter(new MutableClock());
        server.expect(requestTo("http://cvm.test/cad_intermed.zip"))
                .andRespond(withSuccess(zip("CNPJ;DENOM_SOCIAL\n11222333000181;Exemplo\n", false), MediaType.APPLICATION_OCTET_STREAM));
        assertThatThrownBy(() -> invalidHeaders.consultar("11222333000181")).isInstanceOf(InvalidCvmResponseException.class);
    }

    @Test
    void reutilizaSnapshotEAposExpiracaoAtualizaSobDemanda() throws Exception {
        MutableClock clock = new MutableClock();
        CvmParticipantAdapter adapter = adapter(clock);
        server.expect(requestTo("http://cvm.test/cad_intermed.zip"))
                .andRespond(withSuccess(zip("CNPJ;SIT;TP_REGISTRO\n11222333000181;ATIVO;CORRETORA\n", false), MediaType.APPLICATION_OCTET_STREAM));
        server.expect(requestTo("http://cvm.test/cad_intermed.zip"))
                .andRespond(withSuccess(zip("CNPJ;SIT;TP_REGISTRO\n11222333000181;ATIVO;DISTRIBUIDORA\n", false), MediaType.APPLICATION_OCTET_STREAM));

        assertThat(adapter.consultar("11222333000181")).isPresent();
        assertThat(adapter.consultar("11222333000181")).isPresent();
        clock.advance(Duration.ofHours(24));

        assertThat(adapter.consultar("11222333000181")).contains(new CvmParticipantData("11222333000181", "ATIVO", "DISTRIBUIDORA"));
    }

    @Test
    void classificaRateLimitComoIndisponibilidade() {
        CvmParticipantAdapter adapter = adapter(new MutableClock());
        server.expect(requestTo("http://cvm.test/cad_intermed.zip")).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> adapter.consultar("11222333000181"))
                .isInstanceOf(CvmProviderUnavailableException.class);
    }

    @Test
    void decideRegistrosRepetidosIndependentementeDaOrdemDoCsv() throws Exception {
        String invalidThenValid = "CNPJ;SIT;TP_REGISTRO\n11222333000181;INATIVO;BANCO\n11222333000181;ATIVO;CORRETORA\n";
        String validThenInvalid = "CNPJ;SIT;TP_REGISTRO\n11222333000181;ATIVO;CORRETORA\n11222333000181;INATIVO;BANCO\n";

        CvmParticipantAdapter first = adapter(new MutableClock());
        server.expect(requestTo("http://cvm.test/cad_intermed.zip")).andRespond(withSuccess(zip(invalidThenValid, false), MediaType.APPLICATION_OCTET_STREAM));
        Optional<CvmParticipantData> firstResult = first.consultar("11222333000181");
        server.verify();

        CvmParticipantAdapter second = adapter(new MutableClock());
        server.expect(requestTo("http://cvm.test/cad_intermed.zip")).andRespond(withSuccess(zip(validThenInvalid, false), MediaType.APPLICATION_OCTET_STREAM));
        Optional<CvmParticipantData> secondResult = second.consultar("11222333000181");

        assertThat(firstResult).isEqualTo(secondResult);
        assertThat(firstResult).contains(new CvmParticipantData("11222333000181", "ATIVO", "CORRETORA"));
    }

    @Test
    void mantemRepresentanteDeterministicoQuandoNenhumRegistroEhElegivel() throws Exception {
        CvmParticipantAdapter adapter = adapter(new MutableClock());
        server.expect(requestTo("http://cvm.test/cad_intermed.zip")).andRespond(withSuccess(zip("CNPJ;SIT;TP_REGISTRO\n11222333000181;INATIVO;BANCO\n11222333000181;ATIVO;CUSTODIANTE\n", false), MediaType.APPLICATION_OCTET_STREAM));

        assertThat(adapter.consultar("11222333000181"))
                .contains(new CvmParticipantData("11222333000181", "ATIVO", "CUSTODIANTE"));
    }

    @Test
    void classificaConexaoETimeoutComoIndisponibilidade() throws Exception {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        CvmProperties properties = properties(URI.create("http://127.0.0.1:" + closedPort + "/cad_intermed.zip"), Duration.ofMillis(100));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(100));
        factory.setReadTimeout(Duration.ofMillis(100));
        CvmParticipantAdapter connection = new CvmParticipantAdapter(RestClient.builder().requestFactory(factory).build(), properties, Clock.systemUTC());
        assertThatThrownBy(() -> connection.consultar("11222333000181")).isInstanceOf(CvmProviderUnavailableException.class);

        try (ServerSocket slowServer = new ServerSocket(0)) {
            Thread responder = new Thread(() -> {
                try {
                    slowServer.accept();
                    Thread.sleep(500);
                } catch (Exception ignored) {
                }
            });
            responder.start();
            CvmParticipantAdapter timeout = new CvmParticipantAdapter(
                    RestClient.builder().requestFactory(factory).build(),
                    properties(URI.create("http://127.0.0.1:" + slowServer.getLocalPort() + "/cad_intermed.zip"), Duration.ofMillis(100)),
                    Clock.systemUTC());
            assertThatThrownBy(() -> timeout.consultar("11222333000181")).isInstanceOf(CvmProviderUnavailableException.class);
            responder.join(1000);
        }
    }

    private CvmParticipantAdapter adapter(MutableClock clock) {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        return new CvmParticipantAdapter(builder.build(), properties(URI.create("http://cvm.test/cad_intermed.zip"), Duration.ofHours(24)), clock);
    }

    private CvmProperties properties(URI uri, Duration refreshInterval) {
        return new CvmProperties(uri, Duration.ofMillis(100), Duration.ofMillis(100), refreshInterval);
    }

    private byte[] zip(String csv, boolean includeIrrelevantEntry) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream output = new ZipOutputStream(bytes)) {
            if (includeIrrelevantEntry) {
                output.putNextEntry(new ZipEntry("cad_intermed_resp.csv"));
                output.write("IGNORAR".getBytes(StandardCharsets.ISO_8859_1));
                output.closeEntry();
            }
            output.putNextEntry(new ZipEntry("cad_intermed.csv"));
            output.write(csv.getBytes(StandardCharsets.ISO_8859_1));
            output.closeEntry();
        }
        return bytes.toByteArray();
    }

    private static final class MutableClock extends Clock {
        private Instant instant = Instant.parse("2026-09-02T12:00:00Z");

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
