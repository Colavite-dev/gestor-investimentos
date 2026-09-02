package com.colavite.gestor_investimento.integration.cnpj.brasilapi;

import com.colavite.gestor_investimento.exception.CnpjNotFoundException;
import com.colavite.gestor_investimento.exception.CnpjProviderUnavailableException;
import com.colavite.gestor_investimento.exception.InvalidCnpjResponseException;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.validation.CnpjUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class BrasilApiCnpjAdapter implements CnpjDataProvider {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final RestClient restClient;

    public BrasilApiCnpjAdapter(@Qualifier("brasilApiRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CnpjRegistrationData consultar(String cnpj) {
        try {
            BrasilApiCnpjResponse response = restClient.get()
                    .uri("/api/cnpj/v1/{cnpj}", cnpj)
                    .retrieve()
                    .onStatus(status -> status.value() == 404,
                            (request, externalResponse) -> { throw new CnpjNotFoundException(cnpj); })
                    .onStatus(HttpStatusCode::isError,
                            (request, externalResponse) -> { throw new CnpjProviderUnavailableException(); })
                    .body(BrasilApiCnpjResponse.class);

            return map(response, cnpj);
        } catch (CnpjNotFoundException | CnpjProviderUnavailableException | InvalidCnpjResponseException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new CnpjProviderUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new InvalidCnpjResponseException(exception);
        }
    }

    private CnpjRegistrationData map(BrasilApiCnpjResponse response, String requestedCnpj) {
        if (response == null) {
            throw new InvalidCnpjResponseException();
        }

        String responseCnpj = requiredDigits(response.cnpj(), 14);
        if (!requestedCnpj.equals(responseCnpj)) {
            throw new InvalidCnpjResponseException();
        }

        return new CnpjRegistrationData(
                responseCnpj,
                required(response.razaoSocial(), 150),
                optional(response.nomeFantasia(), 150),
                optionalEmail(response.email()),
                optionalPhone(response.telefone()),
                requiredDigits(response.cep(), 8),
                required(response.logradouro(), 150),
                required(response.numero(), 20),
                optional(response.complemento(), 100),
                required(response.bairro(), 100),
                required(response.municipio(), 100),
                requiredUf(response.uf()),
                required(response.situacaoCadastral(), 30)
        );
    }

    private String required(String value, int maxLength) {
        String normalized = optional(value, maxLength);
        if (normalized == null) {
            throw new InvalidCnpjResponseException();
        }
        return normalized;
    }

    private String optional(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new InvalidCnpjResponseException();
        }
        return normalized;
    }

    private String requiredDigits(String value, int length) {
        if (value == null || value.isBlank()) {
            throw new InvalidCnpjResponseException();
        }
        String digits = CnpjUtils.somenteDigitos(value);
        if (digits.length() != length || !value.replaceAll("[.\\-/\\s]", "").matches("\\d+")) {
            throw new InvalidCnpjResponseException();
        }
        return digits;
    }

    private String optionalPhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = CnpjUtils.somenteDigitos(value);
        if (!value.replaceAll("[()\\-\\s]", "").matches("\\d+") || digits.length() < 10 || digits.length() > 11) {
            throw new InvalidCnpjResponseException();
        }
        return digits;
    }

    private String optionalEmail(String value) {
        String email = optional(value, 254);
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidCnpjResponseException();
        }
        return email;
    }

    private String requiredUf(String value) {
        String uf = required(value, 2).toUpperCase(Locale.ROOT);
        if (!uf.matches("[A-Z]{2}")) {
            throw new InvalidCnpjResponseException();
        }
        return uf;
    }
}
