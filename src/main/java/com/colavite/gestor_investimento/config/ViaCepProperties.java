package com.colavite.gestor_investimento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "integration.cep.via-cep")
public record ViaCepProperties(URI baseUrl, Duration connectTimeout, Duration readTimeout) {
}
