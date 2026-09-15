package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CorretoraPersistenceIntegrationTest {
    private static final String CNPJ = "11222333000181";
    @Autowired private CorretoraPersistenceService persistenceService;
    @Autowired private CorretoraRepository repository;
    @Autowired private UsuarioRepository usuarios;
    private Usuario owner;

    @BeforeEach void setUp() { owner = TestUsuarios.persistir(usuarios, "broker-persist"); }
    @AfterEach void cleanUp() { if (owner != null) repository.findByUsuarioIdAndCnpj(owner.getId(), CNPJ).ifPresent(repository::delete); }

    @Test
    void fazRollbackQuandoOFlushDaEscritaFalha() {
        Corretora invalid = new Corretora(CNPJ, null, null, null, null, "01001000", "Praca", "100", null,
                "Se", "Sao Paulo", "SP", "ATIVA", owner);

        assertThatThrownBy(() -> persistenceService.persistir(invalid))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repository.findByUsuarioIdAndCnpj(owner.getId(), CNPJ)).isEmpty();
    }
}
