package com.colavite.gestor_investimento.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CnpjValidatorTest {

    private final CnpjValidator validator = new CnpjValidator();

    @Test
    void deveAceitarCnpjValidoComOuSemMascara() {
        assertThat(validator.isValid("11222333000181", null)).isTrue();
        assertThat(validator.isValid("11.222.333/0001-81", null)).isTrue();
    }

    @Test
    void deveRejeitarFormatoDigitosESequenciasInvalidas() {
        assertThat(validator.isValid(null, null)).isFalse();
        assertThat(validator.isValid("11222333000182", null)).isFalse();
        assertThat(validator.isValid("11.222.333/0001-82", null)).isFalse();
        assertThat(validator.isValid("11111111111111", null)).isFalse();
        assertThat(validator.isValid("11.222.333/000181", null)).isFalse();
    }

    @Test
    void deveNormalizarSomenteDigitos() {
        assertThat(CnpjUtils.somenteDigitos("11.222.333/0001-81")).isEqualTo("11222333000181");
        assertThat(CnpjUtils.somenteDigitos("01001-000")).isEqualTo("01001000");
        assertThat(CnpjUtils.somenteDigitos(null)).isNull();
    }
}
