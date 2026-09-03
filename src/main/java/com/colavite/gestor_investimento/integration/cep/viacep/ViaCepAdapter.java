package com.colavite.gestor_investimento.integration.cep.viacep;

import com.colavite.gestor_investimento.exception.CepNotFoundException;
import com.colavite.gestor_investimento.exception.CepProviderUnavailableException;
import com.colavite.gestor_investimento.exception.InvalidCepResponseException;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import com.colavite.gestor_investimento.integration.cep.CepDataProvider;
import com.colavite.gestor_investimento.validation.CepUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Locale;

@Component
public class ViaCepAdapter implements CepDataProvider {

    private final RestClient restClient;

    public ViaCepAdapter(@Qualifier("viaCepRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CepAddressData consultar(String cep) {
        try {
            ViaCepResponse response = restClient.get().uri("/ws/{cep}/json/", cep).retrieve()
                    .onStatus(HttpStatusCode::isError, (request, externalResponse) -> {
                        throw new CepProviderUnavailableException();
                    }).body(ViaCepResponse.class);
            return map(response, cep);
        } catch (CepNotFoundException | CepProviderUnavailableException | InvalidCepResponseException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new CepProviderUnavailableException(exception);
        } catch (RestClientException exception) {
            throw new InvalidCepResponseException(exception);
        }
    }

    private CepAddressData map(ViaCepResponse response, String requestedCep) {
        if (response == null) throw new InvalidCepResponseException();
        if (Boolean.TRUE.equals(response.erro())) throw new CepNotFoundException();
        String cep = requiredCep(response.cep());
        if (!requestedCep.equals(cep)) throw new InvalidCepResponseException();
        return new CepAddressData(cep, optional(response.logradouro(), 150), optional(response.bairro(), 100),
                required(response.localidade(), 100), requiredUf(response.uf()));
    }

    private String requiredCep(String value) {
        try { return CepUtils.normalizar(value); } catch (IllegalArgumentException exception) { throw new InvalidCepResponseException(exception); }
    }

    private String required(String value, int maxLength) {
        String result = optional(value, maxLength);
        if (result == null) throw new InvalidCepResponseException();
        return result;
    }

    private String optional(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        String result = value.trim();
        if (result.length() > maxLength) throw new InvalidCepResponseException();
        return result;
    }

    private String requiredUf(String value) {
        String uf = required(value, 2).toUpperCase(Locale.ROOT);
        if (!uf.matches("[A-Z]{2}")) throw new InvalidCepResponseException();
        return uf;
    }
}
