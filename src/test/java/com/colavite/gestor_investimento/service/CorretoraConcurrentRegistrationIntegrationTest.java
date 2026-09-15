package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.exception.CnpjDuplicadoException;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import com.colavite.gestor_investimento.integration.cep.CepDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantProvider;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CorretoraConcurrentRegistrationIntegrationTest {
    private static final String CNPJ = "11222333000181";
    @Autowired private CorretoraService service;
    @Autowired private CorretoraRepository repository;
    @Autowired private UsuarioRepository usuarios;
    @MockitoBean private CnpjDataProvider cnpjDataProvider;
    @MockitoBean private CepDataProvider cepDataProvider;
    @MockitoBean private CvmParticipantProvider cvmParticipantProvider;
    private Usuario ownerA;
    private Usuario ownerB;

    @BeforeEach
    void setUp() {
        ownerA = TestUsuarios.persistir(usuarios, "broker-concurrent-a");
        ownerB = TestUsuarios.persistir(usuarios, "broker-concurrent-b");
        when(cnpjDataProvider.consultar(CNPJ)).thenReturn(registrationData());
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        when(cvmParticipantProvider.consultar(CNPJ)).thenReturn(Optional.of(
                new CvmParticipantData(CNPJ, "EM FUNCIONAMENTO NORMAL", "CORRETORA")));
    }

    @AfterEach
    void cleanUp() {
        repository.findByUsuarioIdAndCnpj(ownerA.getId(), CNPJ).ifPresent(repository::delete);
        repository.findByUsuarioIdAndCnpj(ownerB.getId(), CNPJ).ifPresent(repository::delete);
    }

    @Test
    void mesmoOwnerMesmoCnpjResultaEmUmaCriacaoEUmaColisao() throws Exception {
        List<RegistrationResult> results = concurrentRegistrations(ownerA.getId(), ownerA.getId());

        assertThat(results).extracting(RegistrationResult::success).containsExactlyInAnyOrder(true, false);
        assertThat(results).filteredOn(result -> !result.success()).allSatisfy(result ->
                assertThat(result.error()).isInstanceOf(CnpjDuplicadoException.class));
        assertThat(repository.findAllByUsuarioIdOrderByIdAsc(ownerA.getId()).stream()
                .filter(c -> CNPJ.equals(c.getCnpj())).count()).isEqualTo(1);
    }

    @Test
    void ownersDiferentesMesmoCnpjResultamEmDuasCriacoesIndependentes() throws Exception {
        List<RegistrationResult> results = concurrentRegistrations(ownerA.getId(), ownerB.getId());

        assertThat(results).extracting(RegistrationResult::success).containsOnly(true);
        assertThat(repository.findByUsuarioIdAndCnpj(ownerA.getId(), CNPJ)).isPresent();
        assertThat(repository.findByUsuarioIdAndCnpj(ownerB.getId(), CNPJ)).isPresent();
    }

    private List<RegistrationResult> concurrentRegistrations(Long firstOwner, Long secondOwner) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<RegistrationResult> first = executor.submit(() -> register(firstOwner));
            Future<RegistrationResult> second = executor.submit(() -> register(secondOwner));
            return List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    private RegistrationResult register(Long ownerId) {
        try { return new RegistrationResult(true, service.cadastrar(new CorretoraRequest(CNPJ), ownerId), null); }
        catch (RuntimeException exception) { return new RegistrationResult(false, null, exception); }
    }
    private CnpjRegistrationData registrationData() { return new CnpjRegistrationData(CNPJ, "Corretora", null, null, null, "01001000", "Praca", "100", null, "Se", "Sao Paulo", "SP", "ATIVA"); }
    private CepAddressData cepData() { return new CepAddressData("01001000", "Praca", "Se", "Sao Paulo", "SP"); }
    private record RegistrationResult(boolean success, CorretoraResponse response, RuntimeException error) { }
}
