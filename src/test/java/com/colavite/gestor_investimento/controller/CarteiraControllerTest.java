package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.entity.Carteira;
import com.colavite.gestor_investimento.repository.CarteiraRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class CarteiraControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired CarteiraRepository repository;
    @Test void criaListaEConsultaCarteira() throws Exception {
        var result = mockMvc.perform(post("/carteiras").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Reserva\",\"descricao\":\"Longo prazo\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("Reserva")).andReturn();
        long id = repository.findAll().get(0).getId();
        mockMvc.perform(get("/carteiras")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Reserva"));
        mockMvc.perform(get("/carteiras/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
    }
    @Test void converteErrosDeEntradaEConsulta() throws Exception {
        mockMvc.perform(post("/carteiras").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/carteiras/999999")).andExpect(status().isNotFound());
    }
    @Test void converteDuplicidadePara409() throws Exception {
        String body = "{\"nome\":\"Reserva\"}";
        mockMvc.perform(post("/carteiras").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mockMvc.perform(post("/carteiras").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" reserva \"}"))
                .andExpect(status().isConflict());
    }
}
