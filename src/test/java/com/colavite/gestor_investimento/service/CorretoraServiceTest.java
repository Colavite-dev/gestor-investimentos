package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.exception.CnpjDuplicadoException;
import com.colavite.gestor_investimento.exception.CnpjProviderUnavailableException;
import com.colavite.gestor_investimento.exception.CorretoraNotFoundException;
import com.colavite.gestor_investimento.exception.CvmParticipantNotAcceptedException;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorretoraServiceTest {
    private static final long OWNER_A = 101L;
    private static final long OWNER_B = 202L;
    private static final String CNPJ = "11222333000181";

    @Mock private CorretoraRepository repository;
    @Mock private UsuarioRepository usuarios;
    @Mock private CnpjDataProvider cnpjDataProvider;
    @Mock private CepDataProvider cepDataProvider;
    @Mock private CvmParticipantProvider cvmParticipantProvider;
    @Mock private CorretoraPersistenceService persistenceService;
    private CorretoraService service;
    private Usuario ownerA;
    private Usuario ownerB;

    @BeforeEach
    void setUp() {
        ownerA = TestUsuarios.novo("broker-service-a");
        ownerB = TestUsuarios.novo("broker-service-b");
        service = new CorretoraService(repository, cnpjDataProvider, cepDataProvider, cvmParticipantProvider,
                persistenceService, usuarios);
        lenient().when(usuarios.findById(OWNER_A)).thenReturn(Optional.of(ownerA));
        lenient().when(usuarios.findById(OWNER_B)).thenReturn(Optional.of(ownerB));
        lenient().when(cnpjDataProvider.consultar(CNPJ)).thenReturn(registrationData(CNPJ));
        lenient().when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        lenient().when(cvmParticipantProvider.consultar(CNPJ)).thenReturn(Optional.of(activeBroker(CNPJ)));
        lenient().when(persistenceService.persistir(any(Corretora.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void cadastraAssociandoUsuarioAutenticadoEValidaCvmAtiva() {
        when(repository.existsByUsuarioIdAndCnpj(OWNER_A, CNPJ)).thenReturn(false);

        CorretoraResponse response = service.cadastrar(new CorretoraRequest("11.222.333/0001-81"), OWNER_A);

        ArgumentCaptor<Corretora> captor = ArgumentCaptor.forClass(Corretora.class);
        verify(persistenceService).persistir(captor.capture());
        assertThat(captor.getValue().getUsuario()).isSameAs(ownerA);
        assertThat(captor.getValue().isValidadaNaCvm()).isTrue();
        assertThat(response.cnpj()).isEqualTo(CNPJ);
        verify(usuarios).findById(OWNER_A);
    }

    @Test
    void rejeitaDuplicidadeSomenteDoMesmoOwnerAntesDosProviders() {
        when(repository.existsByUsuarioIdAndCnpj(OWNER_A, CNPJ)).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(new CorretoraRequest(CNPJ), OWNER_A))
                .isInstanceOf(CnpjDuplicadoException.class);

        verifyNoInteractions(cnpjDataProvider, cepDataProvider, cvmParticipantProvider, persistenceService, usuarios);
    }

    @Test
    void permiteMesmoCnpjParaOutroOwner() {
        when(repository.existsByUsuarioIdAndCnpj(OWNER_A, CNPJ)).thenReturn(false);
        when(repository.existsByUsuarioIdAndCnpj(OWNER_B, CNPJ)).thenReturn(false);

        service.cadastrar(new CorretoraRequest(CNPJ), OWNER_A);
        service.cadastrar(new CorretoraRequest(CNPJ), OWNER_B);

        ArgumentCaptor<Corretora> captor = ArgumentCaptor.forClass(Corretora.class);
        verify(persistenceService, times(2)).persistir(captor.capture());
        assertThat(captor.getAllValues()).extracting(Corretora::getUsuario).containsExactly(ownerA, ownerB);
    }

    @Test
    void usaConsultasOwnerScopedParaListagemEBuscas() {
        Corretora broker = entity(CNPJ, ownerA);
        when(repository.findAllByUsuarioIdOrderByIdAsc(OWNER_A)).thenReturn(List.of(broker));
        when(repository.findByIdAndUsuarioId(1L, OWNER_A)).thenReturn(Optional.of(broker));
        when(repository.findByUsuarioIdAndCnpj(OWNER_A, CNPJ)).thenReturn(Optional.of(broker));

        assertThat(service.listar(OWNER_A)).hasSize(1);
        assertThat(service.buscarPorId(1L, OWNER_A).cnpj()).isEqualTo(CNPJ);
        assertThat(service.buscarPorCnpj("11.222.333/0001-81", OWNER_A).cnpj()).isEqualTo(CNPJ);
        verify(repository).findByIdAndUsuarioId(1L, OWNER_A);
        verify(repository).findByUsuarioIdAndCnpj(OWNER_A, CNPJ);
    }

    @Test
    void recursoDeOutroOwnerTemMesmoNotFound() {
        when(repository.findByIdAndUsuarioId(1L, OWNER_B)).thenReturn(Optional.empty());
        when(repository.findByUsuarioIdAndCnpj(OWNER_B, CNPJ)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(1L, OWNER_B)).isInstanceOf(CorretoraNotFoundException.class);
        assertThatThrownBy(() -> service.buscarPorCnpj(CNPJ, OWNER_B)).isInstanceOf(CorretoraNotFoundException.class);
    }

    @Test
    void falhaExternaNaoPersiste() {
        when(repository.existsByUsuarioIdAndCnpj(OWNER_A, CNPJ)).thenReturn(false);
        when(cnpjDataProvider.consultar(CNPJ)).thenThrow(new CnpjProviderUnavailableException());

        assertThatThrownBy(() -> service.cadastrar(new CorretoraRequest(CNPJ), OWNER_A))
                .isInstanceOf(CnpjProviderUnavailableException.class);
        verifyNoInteractions(persistenceService);
    }

    @Test
    void cvmIncompativelNaoPersiste() {
        when(repository.existsByUsuarioIdAndCnpj(OWNER_A, CNPJ)).thenReturn(false);
        when(cvmParticipantProvider.consultar(CNPJ)).thenReturn(Optional.of(
                new CvmParticipantData(CNPJ, "EM FUNCIONAMENTO NORMAL", "BANCO COMERCIAL")));

        assertThatThrownBy(() -> service.cadastrar(new CorretoraRequest(CNPJ), OWNER_A))
                .isInstanceOf(CvmParticipantNotAcceptedException.class);
        verifyNoInteractions(persistenceService);
    }

    private Corretora entity(String cnpj, Usuario usuario) {
        return new Corretora(cnpj, "Corretora", null, null, null, "01001000", "Praca", "100", null,
                "Se", "Sao Paulo", "SP", "ATIVA", usuario);
    }
    private CnpjRegistrationData registrationData(String cnpj) {
        return new CnpjRegistrationData(cnpj, "Corretora Oficial S.A.", null, null, null, "01001000",
                "Praca", "100", null, "Se", "Sao Paulo", "SP", "ATIVA");
    }
    private CepAddressData cepData() { return new CepAddressData("01001000", "Praca", "Se", "Sao Paulo", "SP"); }
    private CvmParticipantData activeBroker(String cnpj) {
        return new CvmParticipantData(cnpj, "EM FUNCIONAMENTO NORMAL", "CORRETORA DE TITULOS E VALORES MOBILIARIOS");
    }
}
