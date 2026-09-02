package com.colavite.gestor_investimento;

import com.colavite.gestor_investimento.config.BrasilApiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GestorInvestimentoApplicationTests {

    @Autowired
    private BrasilApiProperties brasilApiProperties;

	@Test
	void contextLoads() {
		assertThat(brasilApiProperties.baseUrl()).isEqualTo(URI.create("http://localhost:1"));
		assertThat(brasilApiProperties.connectTimeout()).isEqualTo(Duration.ofMillis(100));
		assertThat(brasilApiProperties.readTimeout()).isEqualTo(Duration.ofMillis(100));
	}

}
