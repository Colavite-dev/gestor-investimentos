package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import com.colavite.gestor_investimento.integration.cep.CepDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantProvider;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import com.colavite.gestor_investimento.service.CorretoraService;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_IT", matches = "true")
class CorretoraPostgresIT {
    private static final String CNPJ = "11222333000181";
    @Autowired private CorretoraService service;
    @Autowired private CorretoraRepository repository;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private JdbcTemplate jdbcTemplate;
    @MockitoBean private CnpjDataProvider cnpjDataProvider;
    @MockitoBean private CepDataProvider cepDataProvider;
    @MockitoBean private CvmParticipantProvider cvmParticipantProvider;
    private Usuario ownerA;
    private Usuario ownerB;

    @BeforeEach
    void setUp() {
        ownerA = TestUsuarios.persistir(usuarios, "broker-postgres-a");
        ownerB = TestUsuarios.persistir(usuarios, "broker-postgres-b");
        when(cnpjDataProvider.consultar(CNPJ)).thenReturn(new CnpjRegistrationData(CNPJ, "Corretora", null, null, null,
                "01001000", "Praca", "100", null, "Se", "Sao Paulo", "SP", "ATIVA"));
        when(cepDataProvider.consultar("01001000")).thenReturn(new CepAddressData("01001000", "Praca", "Se", "Sao Paulo", "SP"));
        when(cvmParticipantProvider.consultar(CNPJ)).thenReturn(Optional.of(
                new CvmParticipantData(CNPJ, "EM FUNCIONAMENTO NORMAL", "CORRETORA")));
    }

    @AfterEach
    void cleanUp() {
        repository.findByUsuarioIdAndCnpj(ownerA.getId(), CNPJ).ifPresent(repository::delete);
        repository.findByUsuarioIdAndCnpj(ownerB.getId(), CNPJ).ifPresent(repository::delete);
    }

    @Test
    void confirmaFlywayV9EIsolamentoNoPostgres() {
        service.cadastrar(new CorretoraRequest(CNPJ), ownerA.getId());
        service.cadastrar(new CorretoraRequest(CNPJ), ownerB.getId());

        assertThat(repository.findByUsuarioIdAndCnpj(ownerA.getId(), CNPJ)).isPresent();
        assertThat(repository.findByUsuarioIdAndCnpj(ownerB.getId(), CNPJ)).isPresent();
        assertThat(jdbcTemplate.queryForObject("select count(*) from flyway_schema_history where version = '9' and success", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from information_schema.table_constraints where table_name='corretoras' and constraint_name='uk_corretoras_usuario_cnpj'", Integer.class)).isEqualTo(1);
    }
}
