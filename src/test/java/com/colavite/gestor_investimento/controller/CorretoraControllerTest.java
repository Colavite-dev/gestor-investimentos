package com.colavite.gestor_investimento.controller;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CorretoraControllerTest {
    private static final String CNPJ = "11222333000181";
    @Autowired private MockMvc mockMvc;
    @Autowired private CorretoraRepository repository;
    @Autowired private UsuarioRepository usuarios;
    @MockitoBean private CnpjDataProvider cnpjDataProvider;
    @MockitoBean private CepDataProvider cepDataProvider;
    @MockitoBean private CvmParticipantProvider cvmParticipantProvider;
    private Long ownerA;
    private Long ownerB;

    @BeforeEach
    void setUp() {
        ownerA = TestUsuarios.persistir(usuarios, "broker-controller-a").getId();
        ownerB = TestUsuarios.persistir(usuarios, "broker-controller-b").getId();
        stubProviders();
    }

    @Test
    void isolaListagemEConsultasPorIdECnpjEntreUsuarios() throws Exception {
        create(ownerA).andExpect(status().isCreated());
        Corretora brokerA = repository.findByUsuarioIdAndCnpj(ownerA, CNPJ).orElseThrow();

        mockMvc.perform(get("/corretoras").with(user(ownerA.toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].cnpj").value(CNPJ));
        mockMvc.perform(get("/corretoras").with(user(ownerB.toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/corretoras/{id}", brokerA.getId()).with(user(ownerB.toString())))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/corretoras/cnpj/{cnpj}", CNPJ).with(user(ownerB.toString())))
                .andExpect(status().isNotFound());
    }

    @Test
    void permiteMesmoCnpjParaOutroOwnerERejeitaRepeticaoDoMesmoOwner() throws Exception {
        create(ownerA).andExpect(status().isCreated());
        create(ownerB).andExpect(status().isCreated());
        reset(cnpjDataProvider, cepDataProvider, cvmParticipantProvider);

        create(ownerA).andExpect(status().isConflict());
        verifyNoInteractions(cnpjDataProvider, cepDataProvider, cvmParticipantProvider);
        assertThat(repository.findByUsuarioIdAndCnpj(ownerA, CNPJ)).isPresent();
        assertThat(repository.findByUsuarioIdAndCnpj(ownerB, CNPJ)).isPresent();
    }

    @Test
    void rejeitaCampoDeOwnerForjadoMantendoContratoEstrito() throws Exception {
        mockMvc.perform(post("/corretoras").with(user(ownerA.toString())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cnpj\":\"11222333000181\",\"usuarioId\":999}"))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions create(Long owner) throws Exception {
        return mockMvc.perform(post("/corretoras").with(user(owner.toString())).contentType(MediaType.APPLICATION_JSON)
                .content("{\"cnpj\":\"11.222.333/0001-81\"}"));
    }
    private void stubProviders() {
        when(cnpjDataProvider.consultar(CNPJ)).thenReturn(new CnpjRegistrationData(CNPJ, "Corretora Oficial", null, null, null,
                "01001000", "Praca", "100", null, "Se", "Sao Paulo", "SP", "ATIVA"));
        when(cepDataProvider.consultar("01001000")).thenReturn(new CepAddressData("01001000", "Praca", "Se", "Sao Paulo", "SP"));
        when(cvmParticipantProvider.consultar(CNPJ)).thenReturn(Optional.of(
                new CvmParticipantData(CNPJ, "EM FUNCIONAMENTO NORMAL", "CORRETORA")));
    }
}
