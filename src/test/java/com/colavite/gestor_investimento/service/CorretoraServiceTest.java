package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.exception.CnpjDuplicadoException;
import com.colavite.gestor_investimento.exception.CnpjProviderUnavailableException;
import com.colavite.gestor_investimento.exception.CorretoraNotFoundException;
import com.colavite.gestor_investimento.exception.CvmParticipantNotAcceptedException;
import com.colavite.gestor_investimento.exception.InvalidCepResponseException;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import com.colavite.gestor_investimento.integration.cep.CepDataProvider;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantProvider;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CorretoraServiceTest {

    @Mock
    private CorretoraRepository repository;

    @Mock
    private CnpjDataProvider cnpjDataProvider;

    @Mock
    private CepDataProvider cepDataProvider;

    @Mock
    private CvmParticipantProvider cvmParticipantProvider;

    private CorretoraService service;

    @BeforeEach
    void setUp() {
        service = new CorretoraService(repository, cnpjDataProvider, cepDataProvider, cvmParticipantProvider);
        lenient().when(cvmParticipantProvider.consultar("11222333000181"))
                .thenReturn(Optional.of(new CvmParticipantData("11222333000181", "ATIVO", "CORRETORA DE TITULOS E VALORES MOBILIARIOS")));
    }

    @Test
    void deveCadastrarCorretoraNormalizadaEValidadaNaCvm() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        when(repository.saveAndFlush(any(Corretora.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CorretoraResponse response = service.cadastrar(request("11.222.333/0001-81"));

        assertThat(response.cnpj()).isEqualTo("11222333000181");
        assertThat(response.cep()).isEqualTo("01001000");
        assertThat(response.uf()).isEqualTo("SP");
        assertThat(response.validadaNaCvm()).isTrue();
        assertThat(response.razaoSocial()).isEqualTo("Corretora Oficial S.A.");
        assertThat(response.nomeFantasia()).isEqualTo("Corretora Oficial");
        verify(repository).saveAndFlush(any(Corretora.class));
        verify(cnpjDataProvider).consultar("11222333000181");
    }

    @Test
    void deveRejeitarCnpjDuplicadoAntesDoInsert() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(request("11222333000181")))
                .isInstanceOf(CnpjDuplicadoException.class);
        verifyNoInteractions(cnpjDataProvider);
        verifyNoInteractions(cepDataProvider);
        verifyNoInteractions(cvmParticipantProvider);
    }

    @Test
    void deveTraduzirViolacaoConcorrenteDeUnicidade() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        when(repository.saveAndFlush(any(Corretora.class))).thenThrow(new DataIntegrityViolationException("unique"));

        assertThatThrownBy(() -> service.cadastrar(request("11222333000181")))
                .isInstanceOf(CnpjDuplicadoException.class);
    }

    @Test
    void devePropagarFalhaDoProviderSemPersistir() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        when(cnpjDataProvider.consultar("11222333000181"))
                .thenThrow(new CnpjProviderUnavailableException());

        assertThatThrownBy(() -> service.cadastrar(request("11222333000181")))
                .isInstanceOf(CnpjProviderUnavailableException.class);

        verify(repository, never()).saveAndFlush(any(Corretora.class));
    }

    @Test
    void deveEnriquecerCamposDeEnderecoAusentes() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        CnpjRegistrationData data = registrationData("11222333000181");
        data = new CnpjRegistrationData(data.cnpj(), data.razaoSocial(), data.nomeFantasia(), data.email(), data.telefone(),
                "01001-000", null, data.numero(), data.complemento(), null, null, null, data.situacaoCadastral());
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(data);
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        when(repository.saveAndFlush(any(Corretora.class))).thenAnswer(i -> i.getArgument(0));

        CorretoraResponse response = service.cadastrar(request("11222333000181"));

        assertThat(response.cep()).isEqualTo("01001000");
        assertThat(response.logradouro()).isEqualTo("Praça da Sé");
        assertThat(response.bairro()).isEqualTo("Sé");
        assertThat(response.cidade()).isEqualTo("São Paulo");
        assertThat(response.uf()).isEqualTo("SP");
    }

    @Test
    void devePreservarLogradouroEBairroERejeitarConflitoGeografico() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
        when(cepDataProvider.consultar("01001000")).thenReturn(new CepAddressData("01001000", "Outro", "Outro", "Rio", "RJ"));

        assertThatThrownBy(() -> service.cadastrar(request("11222333000181"))).isInstanceOf(InvalidCepResponseException.class);
        verify(repository, never()).saveAndFlush(any(Corretora.class));
    }

    @Test
    void deveListarNaOrdemFornecidaPeloRepository() {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(entity("11222333000181"), entity("45723174000110")));

        assertThat(service.listar()).extracting(CorretoraResponse::cnpj)
                .containsExactly("11222333000181", "45723174000110");
    }

    @Test
    void devePreservarLogradouroEBairroDaFonteCnpj() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
        when(cepDataProvider.consultar("01001000")).thenReturn(new CepAddressData("01001000", "Outro", "Outro", "São Paulo", "SP"));
        when(repository.saveAndFlush(any(Corretora.class))).thenAnswer(i -> i.getArgument(0));
        CorretoraResponse response = service.cadastrar(request("11222333000181"));
        assertThat(response.logradouro()).isEqualTo("Praça da Sé");
        assertThat(response.bairro()).isEqualTo("Sé");
    }

    @Test
    void deveRejeitarParticipanteCvmAusenteInativoOuIncompativelSemPersistir() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        when(cvmParticipantProvider.consultar("11222333000181")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrar(request("11222333000181")))
                .isInstanceOf(CvmParticipantNotAcceptedException.class);
        when(cvmParticipantProvider.consultar("11222333000181"))
                .thenReturn(Optional.of(new CvmParticipantData("11222333000181", "SUSPENSO", "CORRETORA")));
        assertThatThrownBy(() -> service.cadastrar(request("11222333000181")))
                .isInstanceOf(CvmParticipantNotAcceptedException.class);
        when(cvmParticipantProvider.consultar("11222333000181"))
                .thenReturn(Optional.of(new CvmParticipantData("11222333000181", "ATIVO", "BANCO COMERCIAL")));
        assertThatThrownBy(() -> service.cadastrar(request("11222333000181")))
                .isInstanceOf(CvmParticipantNotAcceptedException.class);
        verify(repository, never()).saveAndFlush(any(Corretora.class));
    }

    @Test
    void deveAceitarDistribuidoraAtivaPelaCvm() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        when(cvmParticipantProvider.consultar("11222333000181"))
                .thenReturn(Optional.of(new CvmParticipantData("11222333000181", "ATIVO", "DISTRIBUIDORA DE TITULOS E VALORES MOBILIARIOS")));
        when(repository.saveAndFlush(any(Corretora.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.cadastrar(request("11222333000181")).validadaNaCvm()).isTrue();
    }

    @Test
    void deveBuscarPorIdECnpj() {
        Corretora entity = entity("11222333000181");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.findByCnpj("11222333000181")).thenReturn(Optional.of(entity));

        assertThat(service.buscarPorId(1L).cnpj()).isEqualTo("11222333000181");
        assertThat(service.buscarPorCnpj("11.222.333/0001-81").cnpj()).isEqualTo("11222333000181");
    }

    @Test
    void deveInformarRecursoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.findByCnpj("11222333000181")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L)).isInstanceOf(CorretoraNotFoundException.class);
        assertThatThrownBy(() -> service.buscarPorCnpj("11222333000181"))
                .isInstanceOf(CorretoraNotFoundException.class);
    }

    private CorretoraRequest request(String cnpj) {
        return new CorretoraRequest(cnpj);
    }

    private CnpjRegistrationData registrationData(String cnpj) {
        return new CnpjRegistrationData(
                cnpj,
                "Corretora Oficial S.A.",
                "Corretora Oficial",
                "contato@oficial.example",
                "1133334444",
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

    private CepAddressData cepData() {
        return new CepAddressData("01001000", "Praça da Sé", "Sé", "São Paulo", "SP");
    }
}
