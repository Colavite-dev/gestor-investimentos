package com.colavite.gestor_investimento.service;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CorretoraConcurrentRegistrationIntegrationTest {

    private static final String CNPJ = "11222333000181";

    @Autowired
    private CorretoraService service;

    @Autowired
    private CorretoraRepository repository;

    @MockitoBean
    private CnpjDataProvider cnpjDataProvider;

    @MockitoBean
    private CepDataProvider cepDataProvider;

    @MockitoBean
    private CvmParticipantProvider cvmParticipantProvider;

    @AfterEach
    void cleanUp() {
        repository.findByCnpj(CNPJ).ifPresent(repository::delete);
    }

    @Test
    void persisteUmaCorretoraETraduzAColisaoConcorrente() throws Exception {
        CountDownLatch providersReached = new CountDownLatch(2);
        CountDownLatch releaseProviders = new CountDownLatch(1);
        when(cnpjDataProvider.consultar(CNPJ)).thenAnswer(invocation -> {
            providersReached.countDown();
            assertThat(releaseProviders.await(5, TimeUnit.SECONDS)).isTrue();
            return registrationData();
        });
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        when(cvmParticipantProvider.consultar(CNPJ))
                .thenReturn(Optional.of(new CvmParticipantData(CNPJ, "EM FUNCIONAMENTO NORMAL", "CORRETORA")));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<RegistrationResult>> futures = List.of(
                    executor.submit(this::register),
                    executor.submit(this::register)
            );
            assertThat(providersReached.await(5, TimeUnit.SECONDS)).isTrue();
            releaseProviders.countDown();

            List<RegistrationResult> results = List.of(
                    futures.get(0).get(10, TimeUnit.SECONDS),
                    futures.get(1).get(10, TimeUnit.SECONDS)
            );
            assertThat(results).extracting(RegistrationResult::success).containsExactlyInAnyOrder(true, false);
            assertThat(results).filteredOn(result -> !result.success()).allSatisfy(result ->
                    assertThat(result.error()).isInstanceOf(CnpjDuplicadoException.class));
        } finally {
            executor.shutdownNow();
        }

        assertThat(repository.findAll().stream().filter(corretora -> CNPJ.equals(corretora.getCnpj())).count())
                .isEqualTo(1);
    }

    private RegistrationResult register() {
        try {
            CorretoraResponse response = service.cadastrar(new CorretoraRequest(CNPJ));
            return new RegistrationResult(true, response, null);
        } catch (RuntimeException exception) {
            return new RegistrationResult(false, null, exception);
        }
    }

    private CnpjRegistrationData registrationData() {
        return new CnpjRegistrationData(CNPJ, "Corretora Oficial S.A.", null, null, null,
                "01001000", "Praça da Sé", "100", null, "Sé", "São Paulo", "SP", "ATIVA");
    }

    private CepAddressData cepData() {
        return new CepAddressData("01001000", "Praça da Sé", "Sé", "São Paulo", "SP");
    }

    private record RegistrationResult(boolean success, CorretoraResponse response, RuntimeException error) {
    }
}
