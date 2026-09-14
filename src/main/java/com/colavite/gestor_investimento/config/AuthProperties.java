package com.colavite.gestor_investimento.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(@NotBlank(message = "JWT_SECRET é obrigatório") String jwtSecret) {
}
