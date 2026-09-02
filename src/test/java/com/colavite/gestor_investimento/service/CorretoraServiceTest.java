package com.colavite.gestor_investimento.service;

import com.colavite.gestor_investimento.dto.CorretoraRequest;
import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.exception.CnpjDuplicadoException;
import com.colavite.gestor_investimento.exception.CnpjProviderUnavailableException;
import com.colavite.gestor_investimento.exception.CorretoraNotFoundException;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
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

@ExtendWith(MockitoExtension.class)
class CorretoraServiceTest {

    @Mock
    private CorretoraRepository repository;

    @Mock
    private CnpjDataProvider cnpjDataProvider;

    private CorretoraService service;

    @BeforeEach
    void setUp() {
        service = new CorretoraService(repository, cnpjDataProvider);
    }

    @Test
    void deveCadastrarCorretoraNormalizadaENaoValidadaNaCvm() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
        when(repository.saveAndFlush(any(Corretora.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CorretoraResponse response = service.cadastrar(request("11.222.333/0001-81"));

        assertThat(response.cnpj()).isEqualTo("11222333000181");
        assertThat(response.cep()).isEqualTo("01001000");
        assertThat(response.uf()).isEqualTo("SP");
        assertThat(response.validadaNaCvm()).isFalse();
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
    }

    @Test
    void deveTraduzirViolacaoConcorrenteDeUnicidade() {
        when(repository.existsByCnpj("11222333000181")).thenReturn(false);
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
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
    void deveListarNaOrdemFornecidaPeloRepository() {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(entity("11222333000181"), entity("45723174000110")));

        assertThat(service.listar()).extracting(CorretoraResponse::cnpj)
                .containsExactly("11222333000181", "45723174000110");
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
}
