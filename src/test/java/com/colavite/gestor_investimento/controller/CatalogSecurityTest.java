package com.colavite.gestor_investimento.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogSecurityTest {
    @Autowired MockMvc mockMvc;

    @Test
    void catalogoExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/acoes/catalogo").param("mercado", "BRASIL"))
                .andExpect(status().isUnauthorized());
    }
}
