package com.colavite.gestor_investimento.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "integration.cvm.participants")
public record CvmProperties(
        URI datasetUrl,
        Duration connectTimeout,
        Duration readTimeout,
        Duration refreshInterval
) {
}
