package com.colavite.gestor_investimento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "integration.cnpj.brasil-api")
public record BrasilApiProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}
