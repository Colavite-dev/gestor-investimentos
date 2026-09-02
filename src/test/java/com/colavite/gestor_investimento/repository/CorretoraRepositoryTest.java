package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Corretora;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CorretoraRepositoryTest {

    @Autowired
    private CorretoraRepository repository;

    @Test
    void devePersistirEBuscarPorCnpj() {
        Corretora saved = repository.saveAndFlush(entity("11222333000181"));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.existsByCnpj("11222333000181")).isTrue();
        assertThat(repository.findByCnpj("11222333000181")).contains(saved);
    }

    @Test
    void deveListarPorIdCrescente() {
        Corretora first = repository.saveAndFlush(entity("11222333000181"));
        Corretora second = repository.saveAndFlush(entity("45723174000110"));

        assertThat(repository.findAllByOrderByIdAsc()).containsExactly(first, second);
    }

    @Test
    void deveGarantirUnicidadeNoBanco() {
        repository.saveAndFlush(entity("11222333000181"));

        assertThatThrownBy(() -> repository.saveAndFlush(entity("11222333000181")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Corretora entity(String cnpj) {
        return new Corretora(
                cnpj,
                "Corretora Exemplo S.A.",
                null,
                null,
                null,
                "01001000",
                "Praça da Sé",
                "100",
                null,
                "Sé",
                "São Paulo",
                "SP",
                "ATIVA"
        );
    }
}
