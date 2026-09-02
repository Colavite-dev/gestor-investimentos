package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.integration.cnpj.CnpjDataProvider;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CorretoraPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CnpjDataProvider cnpjDataProvider;

    @Test
    void deveValidarCrudNoPostgresqlComRollback() throws Exception {
        String cnpj = "45723174000110";
        when(cnpjDataProvider.consultar(cnpj)).thenReturn(registrationData(cnpj));

        MvcResult result = mockMvc.perform(post("/corretoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson(cnpj)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.cnpj").value(cnpj))
                .andExpect(jsonPath("$.validadaNaCvm").value(false))
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value(cnpj));

        mockMvc.perform(get("/corretoras/cnpj/{cnpj}", cnpj))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value(cnpj));

        mockMvc.perform(get("/corretoras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].cnpj", hasItem(cnpj)));

        mockMvc.perform(post("/corretoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson(cnpj)))
                .andExpect(status().isConflict());
    }

    private String validJson(String cnpj) {
        return """
                {"cnpj": "%s"}
                """.formatted(cnpj);
    }

    private CnpjRegistrationData registrationData(String cnpj) {
        return new CnpjRegistrationData(
                cnpj,
                "Registro transitório de validação",
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
