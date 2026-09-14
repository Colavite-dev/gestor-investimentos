package com.colavite.gestor_investimento.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityConfigTest {
    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void rejectsMissingInvalidAndTooShortJwtSecret() {
        assertThatThrownBy(() -> securityConfig.jwtSecretKey(new AuthProperties("")))
                .hasMessageContaining("JWT_SECRET é obrigatório");
        assertThatThrownBy(() -> securityConfig.jwtSecretKey(new AuthProperties("not-base64!")))
                .hasMessageContaining("Base64 válido");
        assertThatThrownBy(() -> securityConfig.jwtSecretKey(new AuthProperties("YWFhYQ==")))
                .hasMessageContaining("pelo menos 256 bits");
    }
}
