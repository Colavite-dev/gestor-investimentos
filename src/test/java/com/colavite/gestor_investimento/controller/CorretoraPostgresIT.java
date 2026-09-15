package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.exception.CnpjDuplicadoException;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import com.colavite.gestor_investimento.integration.cep.CepDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantProvider;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import com.colavite.gestor_investimento.service.CorretoraService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_IT", matches = "true")
class CorretoraPostgresIT {

    private static final String COMMIT_CNPJ = "11222333000181";
    private static final String ROLLBACK_CNPJ = "45723174000110";

    @Autowired private MockMvc mockMvc;
    @Autowired private CorretoraService service;
    @Autowired private CorretoraRepository repository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @MockitoBean private CnpjDataProvider cnpjDataProvider;
    @MockitoBean private CepDataProvider cepDataProvider;
    @MockitoBean private CvmParticipantProvider cvmParticipantProvider;

    @BeforeEach
    void setUpProviders() {
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        when(cvmParticipantProvider.consultar(COMMIT_CNPJ)).thenReturn(Optional.of(new CvmParticipantData(COMMIT_CNPJ, "EM FUNCIONAMENTO NORMAL", "CORRETORA")));
        when(cvmParticipantProvider.consultar(ROLLBACK_CNPJ)).thenReturn(Optional.of(new CvmParticipantData(ROLLBACK_CNPJ, "EM FUNCIONAMENTO NORMAL", "CORRETORA")));
    }

    @AfterEach
    void cleanUp() {
        List.of(COMMIT_CNPJ, ROLLBACK_CNPJ).forEach(cnpj -> repository.findByCnpj(cnpj).ifPresent(repository::delete));
    }

    @Test
    void confirmaCommitRealComFlywayEHibernateValidados() throws Exception {
        when(cnpjDataProvider.consultar(COMMIT_CNPJ)).thenReturn(registrationData(COMMIT_CNPJ));

        mockMvc.perform(post("/corretoras").contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(validJson(COMMIT_CNPJ)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.cnpj").value(COMMIT_CNPJ)).andExpect(jsonPath("$.validadaNaCvm").value(true));

        assertThat(repository.findByCnpj(COMMIT_CNPJ)).isPresent();
        assertThat(jdbcTemplate.queryForObject("select count(*) from flyway_schema_history where success", Integer.class)).isEqualTo(6);
    }

    @Test
    void reverteEscritaQuandoPersistenciaFalha() {
        when(cnpjDataProvider.consultar(ROLLBACK_CNPJ)).thenReturn(registrationDataWithMissingCompanyName(ROLLBACK_CNPJ));

        assertThatThrownBy(() -> service.cadastrar(new CorretoraRequest(ROLLBACK_CNPJ))).isInstanceOf(RuntimeException.class);

        assertThat(repository.findByCnpj(ROLLBACK_CNPJ)).isEmpty();
    }

    @Test
    void protegeUnicidadeEmColisaoConcorrente() throws Exception {
        CountDownLatch providersReached = new CountDownLatch(2);
        CountDownLatch releaseProviders = new CountDownLatch(1);
        when(cnpjDataProvider.consultar(COMMIT_CNPJ)).thenAnswer(invocation -> {
            providersReached.countDown();
            assertThat(releaseProviders.await(5, TimeUnit.SECONDS)).isTrue();
            return registrationData(COMMIT_CNPJ);
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<RegistrationResult>> futures = List.of(executor.submit(this::register), executor.submit(this::register));
            assertThat(providersReached.await(5, TimeUnit.SECONDS)).isTrue();
            releaseProviders.countDown();
            List<RegistrationResult> results = List.of(futures.get(0).get(10, TimeUnit.SECONDS), futures.get(1).get(10, TimeUnit.SECONDS));
            assertThat(results).extracting(RegistrationResult::success).containsExactlyInAnyOrder(true, false);
            assertThat(results).filteredOn(result -> !result.success()).allSatisfy(result -> assertThat(result.error()).isInstanceOf(CnpjDuplicadoException.class));
        } finally {
            executor.shutdownNow();
        }

        assertThat(repository.findAll().stream().filter(corretora -> COMMIT_CNPJ.equals(corretora.getCnpj())).count()).isEqualTo(1);
    }

    private RegistrationResult register() {
        try {
            CorretoraResponse response = service.cadastrar(new CorretoraRequest(COMMIT_CNPJ));
            return new RegistrationResult(true, response, null);
        } catch (RuntimeException exception) {
            return new RegistrationResult(false, null, exception);
        }
    }

    private String validJson(String cnpj) { return "{\"cnpj\": \"%s\"}".formatted(cnpj); }
    private CnpjRegistrationData registrationData(String cnpj) { return new CnpjRegistrationData(cnpj, "Corretora PostgreSQL S.A.", null, null, null, "01001000", "Praça da Sé", "100", null, "Sé", "São Paulo", "SP", "ATIVA"); }
    private CnpjRegistrationData registrationDataWithMissingCompanyName(String cnpj) { return new CnpjRegistrationData(cnpj, null, null, null, null, "01001000", "Praça da Sé", "100", null, "Sé", "São Paulo", "SP", "ATIVA"); }
    private CepAddressData cepData() { return new CepAddressData("01001000", "Praça da Sé", "Sé", "São Paulo", "SP"); }
    private record RegistrationResult(boolean success, CorretoraResponse response, RuntimeException error) { }
}
