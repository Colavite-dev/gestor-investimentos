package com.colavite.gestor_investimento.controller;

import org.springframework.security.test.context.support.WithMockUser;

import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Carteira;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Operacao;
import com.colavite.gestor_investimento.entity.TipoOperacao;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import com.colavite.gestor_investimento.repository.CarteiraRepository;
import com.colavite.gestor_investimento.repository.OperacaoRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser
class OperacaoSaldoIntegridadeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CarteiraRepository carteiras;
    @Autowired private AcaoRepository acoes;
    @Autowired private OperacaoRepository operacoes;
    @Autowired private UsuarioRepository usuarios;

    @Test
    void rejeitaVendaExcedenteCom422SemPersistir() throws Exception {
        var owner = TestUsuarios.persistir(usuarios, "saldo-controller");
        Carteira carteira = carteiras.saveAndFlush(new Carteira("Carteira saldo", "CARTEIRA SALDO", null, owner));
        Acao acao = acoes.saveAndFlush(new Acao("PETR4", "Petrobras", Mercado.BRASIL, new BigDecimal("30"), Instant.now()));
        operacoes.saveAndFlush(new Operacao(carteira, acao, TipoOperacao.COMPRA, new BigDecimal("10"), new BigDecimal("30"), Instant.parse("2026-09-01T12:00:00Z")));

        String corpo = "{\"carteiraId\":" + carteira.getId() + ",\"acaoId\":" + acao.getId()
                + ",\"tipo\":\"VENDA\",\"quantidade\":11,\"precoUnitario\":30,\"dataOperacao\":\"2026-09-02T12:00:00Z\"}";

        mockMvc.perform(post("/operacoes").with(user(owner.getId().toString())).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Saldo insuficiente para realizar a venda"));

        assertThat(operacoes.findByCarteiraIdAndAcaoIdOrderByDataOperacaoAscIdAsc(carteira.getId(), acao.getId())).hasSize(1);
    }
}
