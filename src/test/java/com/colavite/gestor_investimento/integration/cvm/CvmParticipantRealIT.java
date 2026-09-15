package com.colavite.gestor_investimento.integration.cvm;

import com.colavite.gestor_investimento.config.CvmProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "runCvmRealIT", matches = "true")
class CvmParticipantRealIT {

    @Test
    void consultaDatasetOficialAtualDaCvm() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(20));
        CvmParticipantAdapter adapter = new CvmParticipantAdapter(
                RestClient.builder().requestFactory(factory).build(),
                new CvmProperties(
                        URI.create("https://dados.cvm.gov.br/dados/INTERMED/CAD/DADOS/cad_intermed.zip"),
                        Duration.ofSeconds(5), Duration.ofSeconds(20), Duration.ofHours(24)),
                Clock.systemUTC());

        CvmParticipantData participant = adapter.consultar("02332886000104").orElseThrow();

        assertThat(CvmParticipantEligibility.isEligible(participant)).isTrue();
    }
}
