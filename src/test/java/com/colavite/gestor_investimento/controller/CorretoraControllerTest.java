package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.exception.CnpjNotFoundException;
import com.colavite.gestor_investimento.exception.CnpjProviderUnavailableException;
import com.colavite.gestor_investimento.exception.InvalidCnpjResponseException;
import com.colavite.gestor_investimento.exception.CepNotFoundException;
import com.colavite.gestor_investimento.exception.CepProviderUnavailableException;
import com.colavite.gestor_investimento.exception.InvalidCepResponseException;
import com.colavite.gestor_investimento.exception.CvmParticipantNotAcceptedException;
import com.colavite.gestor_investimento.exception.CvmProviderUnavailableException;
import com.colavite.gestor_investimento.exception.InvalidCvmResponseException;
import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.integration.cep.CepAddressData;
import com.colavite.gestor_investimento.integration.cep.CepDataProvider;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantData;
import com.colavite.gestor_investimento.integration.cvm.CvmParticipantProvider;
import com.colavite.gestor_investimento.repository.CorretoraRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CorretoraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorretoraRepository repository;

    @MockitoBean
    private CnpjDataProvider cnpjDataProvider;

    @MockitoBean
    private CepDataProvider cepDataProvider;

    @MockitoBean
    private CvmParticipantProvider cvmParticipantProvider;

    @Test
    void deveCadastrarSomenteComCnpjEUsarDadosDoProvider() throws Exception {
        when(cnpjDataProvider.consultar("11222333000181"))
                .thenReturn(registrationData("11222333000181"));
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        acceptCvm();

        mockMvc.perform(post("/corretoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("11.222.333/0001-81")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(".*/corretoras/\\d+")))
                .andExpect(jsonPath("$.cnpj").value("11222333000181"))
                .andExpect(jsonPath("$.razaoSocial").value("Corretora Oficial S.A."))
                .andExpect(jsonPath("$.cep").value("01001000"))
                .andExpect(jsonPath("$.uf").value("SP"))
                .andExpect(jsonPath("$.validadaNaCvm").value(true))
                .andExpect(jsonPath("$.dataCadastro").exists());
    }

    @Test
    void deveRetornarConflitoSemConsultarProviderParaDuplicidade() throws Exception {
        repository.saveAndFlush(entity("11222333000181"));

        mockMvc.perform(post("/corretoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("11222333000181")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/corretoras"));

        verifyNoInteractions(cnpjDataProvider);
        verifyNoInteractions(cepDataProvider);
        verifyNoInteractions(cvmParticipantProvider);
    }

    @Test
    void deveRejeitarCnpjInvalidoSemConsultarProvider() throws Exception {
        mockMvc.perform(post("/corretoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cnpj": "11111111111111"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.cnpj").exists());

        verifyNoInteractions(cnpjDataProvider);
    }

    @Test
    void deveRejeitarPropriedadeDesconhecidaDoRequest() throws Exception {
        mockMvc.perform(post("/corretoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cnpj": "11222333000181",
                                  "razaoSocial": "Tentativa de sobrescrita"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido"));

        verifyNoInteractions(cnpjDataProvider);
    }

    @Test
    void deveMapearCnpjNaoEncontradoPara422() throws Exception {
        when(cnpjDataProvider.consultar("11222333000181"))
                .thenThrow(new CnpjNotFoundException("11222333000181"));

        mockMvc.perform(post("/corretoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("11222333000181")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("BrasilAPI"))));
    }

    @Test
    void deveMapearRespostaInvalidaPara502() throws Exception {
        when(cnpjDataProvider.consultar("11222333000181"))
                .thenThrow(new InvalidCnpjResponseException());

        mockMvc.perform(post("/corretoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("11222333000181")))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }

    @Test
    void deveMapearIndisponibilidadePara503() throws Exception {
        when(cnpjDataProvider.consultar("11222333000181"))
                .thenThrow(new CnpjProviderUnavailableException());

        mockMvc.perform(post("/corretoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson("11222333000181")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void deveListarBuscarPorIdECnpj() throws Exception {
        Corretora first = repository.saveAndFlush(entity("11222333000181"));
        Corretora second = repository.saveAndFlush(entity("45723174000110"));

        mockMvc.perform(get("/corretoras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(first.getId()))
                .andExpect(jsonPath("$[1].id").value(second.getId()));

        mockMvc.perform(get("/corretoras/{id}", first.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value("11222333000181"));

        mockMvc.perform(get("/corretoras/cnpj/{cnpj}", "45723174000110"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(second.getId()));
    }

    @Test
    void deveMapearErrosDeCepPara422502E503() throws Exception {
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
        when(cepDataProvider.consultar("01001000")).thenThrow(new CepNotFoundException());
        mockMvc.perform(post("/corretoras").contentType(MediaType.APPLICATION_JSON).content(validJson("11222333000181"))).andExpect(status().isUnprocessableEntity());
        reset(cepDataProvider);
        when(cepDataProvider.consultar("01001000")).thenThrow(new InvalidCepResponseException());
        mockMvc.perform(post("/corretoras").contentType(MediaType.APPLICATION_JSON).content(validJson("11222333000181"))).andExpect(status().isBadGateway());
        reset(cepDataProvider);
        when(cepDataProvider.consultar("01001000")).thenThrow(new CepProviderUnavailableException());
        mockMvc.perform(post("/corretoras").contentType(MediaType.APPLICATION_JSON).content(validJson("11222333000181"))).andExpect(status().isServiceUnavailable());
    }

    @Test
    void deveMapearErrosDaCvmPara422502E503() throws Exception {
        when(cnpjDataProvider.consultar("11222333000181")).thenReturn(registrationData("11222333000181"));
        when(cepDataProvider.consultar("01001000")).thenReturn(cepData());
        when(cvmParticipantProvider.consultar("11222333000181")).thenThrow(new CvmParticipantNotAcceptedException());
        mockMvc.perform(post("/corretoras").contentType(MediaType.APPLICATION_JSON).content(validJson("11222333000181")))
                .andExpect(status().isUnprocessableEntity());
        reset(cvmParticipantProvider);
        when(cvmParticipantProvider.consultar("11222333000181")).thenThrow(new InvalidCvmResponseException());
        mockMvc.perform(post("/corretoras").contentType(MediaType.APPLICATION_JSON).content(validJson("11222333000181")))
                .andExpect(status().isBadGateway());
        reset(cvmParticipantProvider);
        when(cvmParticipantProvider.consultar("11222333000181")).thenThrow(new CvmProviderUnavailableException());
        mockMvc.perform(post("/corretoras").contentType(MediaType.APPLICATION_JSON).content(validJson("11222333000181")))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void deveRetornarNotFoundParaConsultasAusentes() throws Exception {
        mockMvc.perform(get("/corretoras/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(get("/corretoras/cnpj/{cnpj}", "11222333000181"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveValidarParametrosDePath() throws Exception {
        mockMvc.perform(get("/corretoras/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.id").exists());

        mockMvc.perform(get("/corretoras/cnpj/{cnpj}", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.cnpj").exists());
    }

    private String validJson(String cnpj) {
        return """
                {"cnpj": "%s"}
                """.formatted(cnpj);
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

    private void acceptCvm() {
        when(cvmParticipantProvider.consultar("11222333000181"))
                .thenReturn(Optional.of(new CvmParticipantData("11222333000181", "ATIVO", "CORRETORA")));
    }
}
