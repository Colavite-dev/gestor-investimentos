package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.exception.CnpjDuplicadoException;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorretoraPersistenceServiceTest {
    @Mock private CorretoraRepository repository;
    private CorretoraPersistenceService service;

    @BeforeEach void setUp() { service = new CorretoraPersistenceService(repository); }

    @Test
    void traduzSomenteViolacaoDaConstraintCompostaDoOwnerECnpj() {
        when(repository.saveAndFlush(any(Corretora.class))).thenThrow(uniqueViolation("uk_corretoras_usuario_cnpj"));
        assertThatThrownBy(() -> service.persistir(corretora())).isInstanceOf(CnpjDuplicadoException.class);
    }

    @Test
    void propagaOutraViolacaoDeIntegridade() {
        DataIntegrityViolationException violation = uniqueViolation("uk_outra_regra");
        when(repository.saveAndFlush(any(Corretora.class))).thenThrow(violation);
        assertThatThrownBy(() -> service.persistir(corretora())).isSameAs(violation);
    }

    private DataIntegrityViolationException uniqueViolation(String constraintName) {
        SQLException sqlException = new SQLException("duplicate", "23505");
        return new DataIntegrityViolationException("duplicate",
                new ConstraintViolationException("duplicate", sqlException, constraintName));
    }

    private Corretora corretora() {
        return new Corretora("11222333000181", "Corretora", null, null, null, "01001000", "Praca", "100",
                null, "Se", "Sao Paulo", "SP", "ATIVA", TestUsuarios.novo("broker-persistence"));
    }
}
