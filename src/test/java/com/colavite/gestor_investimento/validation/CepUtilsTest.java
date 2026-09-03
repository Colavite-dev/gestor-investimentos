package com.colavite.gestor_investimento.validation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CepUtilsTest {
    @Test void normalizaMascara() { assertThat(CepUtils.normalizar("01001-000")).isEqualTo("01001000"); }
    @Test void rejeitaInvalido() { assertThatIllegalArgumentException().isThrownBy(() -> CepUtils.normalizar("abc")); }
}
