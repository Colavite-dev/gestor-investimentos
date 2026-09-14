package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.entity.*;
import com.colavite.gestor_investimento.repository.*;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OperacaoControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired CarteiraRepository carteiras;
    @Autowired AcaoRepository acoes;
    @Autowired OperacaoRepository operacoes;
    @Autowired UsuarioRepository usuarios;

    @Test
    void criaEConsultaOperacao() throws Exception {
        var owner = TestUsuarios.persistir(usuarios, "operacao-api");
        var carteira = carteiras.saveAndFlush(new Carteira("Carteira API", "CARTEIRA API", null, owner));
        var acao = acoes.saveAndFlush(new Acao("PETR4", "Petrobras", Mercado.BRASIL, new BigDecimal("35"), Instant.now()));
        var result = mockMvc.perform(post("/operacoes").with(user(owner.getId().toString())).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(carteira.getId(), acao.getId(), "10", "30")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.moeda").value("BRL")).andExpect(jsonPath("$.precoUnitario").value(30)).andReturn();
        var id = Long.valueOf(result.getResponse().getHeader("Location").replaceAll(".*/", ""));
        mockMvc.perform(get("/operacoes/{id}", id).with(user(owner.getId().toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("COMPRA"));
        mockMvc.perform(get("/carteiras/{id}/operacoes", carteira.getId()).with(user(owner.getId().toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].acaoId").value(acao.getId()));
    }

    @Test
    void rejeitaRequestInvalidoENaoEncontrado() throws Exception {
        var owner = TestUsuarios.persistir(usuarios, "operacao-invalid");
        mockMvc.perform(post("/operacoes").with(user(owner.getId().toString())).contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"COMPRA\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/operacoes/999999").with(user(owner.getId().toString()))).andExpect(status().isNotFound());
    }

    @Test
    void aceitaQuantidadeFracionariaEPrecoComOitoCasas() throws Exception {
        var owner = TestUsuarios.persistir(usuarios, "operacao-decimal");
        var carteira = carteiras.saveAndFlush(new Carteira("Carteira decimal", "CARTEIRA DECIMAL", null, owner));
        var acao = acoes.saveAndFlush(new Acao("WEGE3", "Weg", Mercado.BRASIL, new BigDecimal("30"), Instant.now()));
        mockMvc.perform(post("/operacoes").with(user(owner.getId().toString())).contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(carteira.getId(), acao.getId(), "0.12345678", "100.00000001")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.quantidade").value(0.12345678));
    }

    @Test
    void rejeitaValoresNumericosForaDaCapacidadeOuNaoPositivos() throws Exception {
        var owner = TestUsuarios.persistir(usuarios, "operacao-limits");
        var carteira = carteiras.saveAndFlush(new Carteira("Carteira limites", "CARTEIRA LIMITES", null, owner));
        var acao = acoes.saveAndFlush(new Acao("ITUB4", "Itau", Mercado.BRASIL, new BigDecimal("30"), Instant.now()));
        long antes = operacoes.count();
        for (String valores : new String[]{"0.123456789,30", "100000000000,30", "0,30", "-1,30", "1,30.123456789", "1,100000000000", "1,0", "1,-30"}) {
            String[] partes = valores.split(",");
            mockMvc.perform(post("/operacoes").with(user(owner.getId().toString())).contentType(MediaType.APPLICATION_JSON)
                            .content(corpo(carteira.getId(), acao.getId(), partes[0], partes[1])))
                    .andExpect(status().isBadRequest());
        }
        assertThat(operacoes.count()).isEqualTo(antes);
    }

    private String corpo(Long carteiraId, Long acaoId, String quantidade, String preco) {
        return "{\"carteiraId\":" + carteiraId + ",\"acaoId\":" + acaoId
                + ",\"tipo\":\"COMPRA\",\"quantidade\":" + quantidade + ",\"precoUnitario\":" + preco
                + ",\"dataOperacao\":\"2026-09-01T12:00:00Z\"}";
    }
}
