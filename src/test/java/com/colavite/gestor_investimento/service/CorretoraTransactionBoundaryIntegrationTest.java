package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import com.colavite.gestor_investimento.integration.cep.CepDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantProvider;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CorretoraTransactionBoundaryIntegrationTest {
    private static final String CNPJ = "11222333000181";
    @Autowired private CorretoraService service;
    @Autowired private UsuarioRepository usuarios;
    @MockitoBean private CorretoraRepository repository;
    @MockitoBean private CnpjDataProvider cnpjDataProvider;
    @MockitoBean private CepDataProvider cepDataProvider;
    @MockitoBean private CvmParticipantProvider cvmParticipantProvider;
    private Usuario owner;

    @BeforeEach void setUp() { owner = TestUsuarios.persistir(usuarios, "broker-transaction"); }

    @Test
    void consultaProvidersForaDaTransacaoEGravaDentroDaTransacaoCurta() {
        when(repository.existsByUsuarioIdAndCnpj(owner.getId(), CNPJ)).thenAnswer(i -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse(); return false;
        });
        when(cnpjDataProvider.consultar(CNPJ)).thenAnswer(i -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse(); return registrationData();
        });
        when(cepDataProvider.consultar("01001000")).thenAnswer(i -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse(); return cepData();
        });
        when(cvmParticipantProvider.consultar(CNPJ)).thenAnswer(i -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return Optional.of(new CvmParticipantData(CNPJ, "EM FUNCIONAMENTO NORMAL", "CORRETORA"));
        });
        when(repository.saveAndFlush(any(Corretora.class))).thenAnswer(i -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue(); return i.getArgument(0);
        });

        assertThat(service.cadastrar(new CorretoraRequest(CNPJ), owner.getId()).validadaNaCvm()).isTrue();
    }
    private CnpjRegistrationData registrationData() { return new CnpjRegistrationData(CNPJ, "Corretora", null, null, null, "01001000", "Praca", "100", null, "Se", "Sao Paulo", "SP", "ATIVA"); }
    private CepAddressData cepData() { return new CepAddressData("01001000", "Praca", "Se", "Sao Paulo", "SP"); }
}
