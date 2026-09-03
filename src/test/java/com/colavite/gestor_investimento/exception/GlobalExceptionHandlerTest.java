package com.colavite.gestor_investimento.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/corretoras");

    @Test
    void deveRepresentarCnpjNaoEncontradoComo422() {
        ResponseEntity<ApiErrorResponse> response = handler.handleCnpjNotFound(
                new CnpjNotFoundException("11222333000181"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).doesNotContain("BrasilAPI");
    }

    @Test
    void deveRepresentarRespostaInvalidaComo502SemCausaExterna() {
        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidCnpjResponse(
                new InvalidCnpjResponseException(new IllegalArgumentException("payload externo sensível")), request);

        assertThat(response.getStatusCode().value()).isEqualTo(502);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("A fonte cadastral retornou dados inválidos");
        assertThat(response.getBody().message()).doesNotContain("payload externo sensível");
    }

    @Test
    void deveRepresentarIndisponibilidadeComo503SemCausaExterna() {
        ResponseEntity<ApiErrorResponse> response = handler.handleCnpjProviderUnavailable(
                new CnpjProviderUnavailableException(new RuntimeException("detalhe remoto")), request);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("A fonte cadastral está temporariamente indisponível");
        assertThat(response.getBody().message()).doesNotContain("detalhe remoto");
    }

    @Test
    void deveRepresentarErrosDeCepSemDetalhesExternos() {
        ResponseEntity<ApiErrorResponse> notFound = handler.handleCepNotFound(new CepNotFoundException(), request);
        ResponseEntity<ApiErrorResponse> invalid = handler.handleInvalidCepResponse(new InvalidCepResponseException(new RuntimeException("corpo remoto")), request);
        ResponseEntity<ApiErrorResponse> unavailable = handler.handleCepProviderUnavailable(new CepProviderUnavailableException(new RuntimeException("host remoto")), request);
        assertThat(notFound.getStatusCode().value()).isEqualTo(422);
        assertThat(invalid.getStatusCode().value()).isEqualTo(502);
        assertThat(unavailable.getStatusCode().value()).isEqualTo(503);
        assertThat(invalid.getBody().message()).doesNotContain("corpo remoto");
        assertThat(unavailable.getBody().message()).doesNotContain("host remoto");
    }

    @Test
    void deveRepresentarErrosDaCvmSemDetalhesExternos() {
        ResponseEntity<ApiErrorResponse> rejected = handler.handleCvmParticipantNotAccepted(new CvmParticipantNotAcceptedException(), request);
        ResponseEntity<ApiErrorResponse> invalid = handler.handleInvalidCvmResponse(new InvalidCvmResponseException(new RuntimeException("zip remoto")), request);
        ResponseEntity<ApiErrorResponse> unavailable = handler.handleCvmProviderUnavailable(new CvmProviderUnavailableException(new RuntimeException("host remoto")), request);
        assertThat(rejected.getStatusCode().value()).isEqualTo(422);
        assertThat(invalid.getStatusCode().value()).isEqualTo(502);
        assertThat(unavailable.getStatusCode().value()).isEqualTo(503);
        assertThat(invalid.getBody().message()).doesNotContain("zip remoto");
        assertThat(unavailable.getBody().message()).doesNotContain("host remoto");
    }
}
