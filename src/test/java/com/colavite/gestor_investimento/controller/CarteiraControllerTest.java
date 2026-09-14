package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.repository.CarteiraRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CarteiraControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired CarteiraRepository repository;
    @Autowired UsuarioRepository usuarios;
    private Long usuarioId;

    @BeforeEach
    void setUp() {
        usuarioId = TestUsuarios.persistir(usuarios, "carteira-controller").getId();
    }

    @Test
    void criaListaEConsultaCarteira() throws Exception {
        mockMvc.perform(post("/carteiras").with(user(usuarioId.toString())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Reserva\",\"descricao\":\"Longo prazo\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("Reserva"));
        long id = repository.findAllByUsuarioIdOrderByIdAsc(usuarioId).get(0).getId();
        mockMvc.perform(get("/carteiras").with(user(usuarioId.toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Reserva"));
        mockMvc.perform(get("/carteiras/{id}", id).with(user(usuarioId.toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void converteErrosDeEntradaEConsulta() throws Exception {
        mockMvc.perform(post("/carteiras").with(user(usuarioId.toString())).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/carteiras/999999").with(user(usuarioId.toString()))).andExpect(status().isNotFound());
    }

    @Test
    void converteDuplicidadePara409() throws Exception {
        String body = "{\"nome\":\"Reserva\"}";
        mockMvc.perform(post("/carteiras").with(user(usuarioId.toString())).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/carteiras").with(user(usuarioId.toString())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\" reserva \"}"))
                .andExpect(status().isConflict());
    }
}
