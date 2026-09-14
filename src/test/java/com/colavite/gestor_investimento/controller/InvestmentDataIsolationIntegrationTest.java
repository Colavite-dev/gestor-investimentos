package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.entity.*;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InvestmentDataIsolationIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired AcaoRepository acoes;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;

    @Test
    void isolatesWalletOperationsPositionsSummaryAndValuationWhileSharingMasterAsset() throws Exception {
        var userA = user("isolation-a", Role.USER);
        var userB = user("isolation-b", Role.USER);
        var admin = user("isolation-admin", Role.ADMIN);
        var petr3 = acoes.saveAndFlush(new Acao("PETR3", "Petrobras", Mercado.BRASIL, new BigDecimal("35"), Instant.now()));
        String tokenA = login(userA.getUsername());
        String tokenB = login(userB.getUsername());
        String adminToken = login(admin.getUsername());

        long walletA = createWallet(tokenA, "Carteira compartilhável");
        long operationA = createPurchase(tokenA, walletA, petr3.getId(), "10", "30");

        mockMvc.perform(get("/carteiras").header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        assertPrivateResourceIsHidden(tokenB, "/carteiras/" + walletA);
        assertPrivateResourceIsHidden(tokenB, "/carteiras/" + walletA + "/operacoes");
        assertPrivateResourceIsHidden(tokenB, "/carteiras/" + walletA + "/posicoes");
        assertPrivateResourceIsHidden(tokenB, "/carteiras/" + walletA + "/resumo");
        assertPrivateResourceIsHidden(tokenB, "/operacoes/" + operationA);
        mockMvc.perform(post("/operacoes").header("Authorization", bearer(tokenB)).contentType(MediaType.APPLICATION_JSON)
                        .content(operationBody(walletA, petr3.getId(), "1", "31")))
                .andExpect(status().isNotFound());
        assertPrivateResourceIsHidden(adminToken, "/carteiras/" + walletA);

        long walletB = createWallet(tokenB, "Carteira compartilhável");
        createPurchase(tokenB, walletB, petr3.getId(), "2", "34");
        mockMvc.perform(get("/carteiras/{id}/posicoes", walletA).header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].ticker").value("PETR3"))
                .andExpect(jsonPath("$[0].quantidade").value(10)).andExpect(jsonPath("$[0].precoMedio").value(30));
        mockMvc.perform(get("/carteiras/{id}/posicoes", walletB).header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].ticker").value("PETR3"))
                .andExpect(jsonPath("$[0].quantidade").value(2)).andExpect(jsonPath("$[0].precoMedio").value(34));
        mockMvc.perform(get("/acoes/{id}", petr3.getId()).header("Authorization", bearer(tokenA))).andExpect(status().isOk());
        mockMvc.perform(get("/acoes/{id}", petr3.getId()).header("Authorization", bearer(tokenB))).andExpect(status().isOk());
    }

    private Usuario user(String username, Role role) {
        return usuarios.saveAndFlush(new Usuario(username, username, username + "@example.test", passwordEncoder.encode("test-password"), role));
    }

    private String login(String username) throws Exception {
        String response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asString();
    }

    private long createWallet(String token, String name) throws Exception {
        String location = mockMvc.perform(post("/carteiras").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + name + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
        return Long.parseLong(location.replaceAll(".*/", ""));
    }

    private long createPurchase(String token, long walletId, long stockId, String quantity, String price) throws Exception {
        String location = mockMvc.perform(post("/operacoes").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(operationBody(walletId, stockId, quantity, price)))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
        return Long.parseLong(location.replaceAll(".*/", ""));
    }

    private String operationBody(long walletId, long stockId, String quantity, String price) {
        return "{\"carteiraId\":" + walletId + ",\"acaoId\":" + stockId + ",\"tipo\":\"COMPRA\",\"quantidade\":"
                + quantity + ",\"precoUnitario\":" + price + ",\"dataOperacao\":\"2026-09-10T12:00:00Z\"}";
    }

    private void assertPrivateResourceIsHidden(String token, String path) throws Exception {
        mockMvc.perform(get(path).header("Authorization", bearer(token))).andExpect(status().isNotFound());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
