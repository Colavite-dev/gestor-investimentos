package com.colavite.gestor_investimento.integration.cep.viacep;

import com.colavite.gestor_investimento.config.ViaCepConfig;
import com.colavite.gestor_investimento.config.ViaCepProperties;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ViaCepRealIT {
    @Test void deveConsultarViaCepDeFormaControlada() {
        RestClient client = new ViaCepConfig().viaCepRestClient(new ViaCepProperties(URI.create("https://viacep.com.br"), Duration.ofSeconds(3), Duration.ofSeconds(8)));
        CepAddressData data = new ViaCepAdapter(client).consultar("01001000");
        assertThat(data.cep()).isEqualTo("01001000");
        assertThat(data.cidade()).isNotBlank();
        assertThat(data.uf()).isEqualTo("SP");
    }
}
