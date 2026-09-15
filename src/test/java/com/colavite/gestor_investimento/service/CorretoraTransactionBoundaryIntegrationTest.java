package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import com.colavite.gestor_investimento.integration.cep.CepDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantProvider;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CorretoraTransactionBoundaryIntegrationTest {

    private static final String CNPJ = "11222333000181";

    @Autowired
    private CorretoraService service;

    @MockitoBean
    private CorretoraRepository repository;

    @MockitoBean
    private CnpjDataProvider cnpjDataProvider;

    @MockitoBean
    private CepDataProvider cepDataProvider;

    @MockitoBean
    private CvmParticipantProvider cvmParticipantProvider;

    @Test
    void consultaProvidersForaDaTransacaoEGravaDentroDaTransacaoCurta() {
        when(repository.existsByCnpj(CNPJ)).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return false;
        });
        when(cnpjDataProvider.consultar(CNPJ)).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return registrationData();
        });
        when(cepDataProvider.consultar("01001000")).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return cepData();
        });
        when(cvmParticipantProvider.consultar(CNPJ)).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return Optional.of(new CvmParticipantData(CNPJ, "EM FUNCIONAMENTO NORMAL", "CORRETORA"));
        });
        when(repository.saveAndFlush(any(Corretora.class))).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            return invocation.getArgument(0);
        });

        assertThat(service.cadastrar(new CorretoraRequest(CNPJ)).validadaNaCvm()).isTrue();
    }

    private CnpjRegistrationData registrationData() {
        return new CnpjRegistrationData(CNPJ, "Corretora Oficial S.A.", null, null, null,
                "01001000", "Praça da Sé", "100", null, "Sé", "São Paulo", "SP", "ATIVA");
    }

    private CepAddressData cepData() {
        return new CepAddressData("01001000", "Praça da Sé", "Sé", "São Paulo", "SP");
    }
}
