package com.colavite.gestor_investimento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "integration.stock.brapi")
public record BrapiProperties(URI baseUrl, Duration connectTimeout, Duration readTimeout, String token) {
}
