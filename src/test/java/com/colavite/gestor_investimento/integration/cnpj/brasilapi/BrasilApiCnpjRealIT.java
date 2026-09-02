package com.colavite.gestor_investimento.integration.cnpj.brasilapi;

import com.colavite.gestor_investimento.config.BrasilApiConfig;
import com.colavite.gestor_investimento.config.BrasilApiProperties;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class BrasilApiCnpjRealIT {

    @Test
    void deveConsultarContratoRealDeFormaControlada() {
        BrasilApiProperties properties = new BrasilApiProperties(
                URI.create("https://brasilapi.com.br"),
                Duration.ofSeconds(3),
                Duration.ofSeconds(8)
        );
        RestClient client = new BrasilApiConfig().brasilApiRestClient(properties);
        BrasilApiCnpjAdapter adapter = new BrasilApiCnpjAdapter(client);

        CnpjRegistrationData data = adapter.consultar("19131243000197");

        assertThat(data.cnpj()).isEqualTo("19131243000197");
        assertThat(data.razaoSocial()).isNotBlank();
        assertThat(data.cep()).hasSize(8);
        assertThat(data.uf()).hasSize(2);
        assertThat(data.situacaoCadastral()).isNotBlank();

        System.out.printf(
                "BrasilAPI validada: cnpj=%s, razaoSocial=%s, uf=%s, situacao=%s%n",
                data.cnpj(), data.razaoSocial(), data.uf(), data.situacaoCadastral()
        );
    }
}
