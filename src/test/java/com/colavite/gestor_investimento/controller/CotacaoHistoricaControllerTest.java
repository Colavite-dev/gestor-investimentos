package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.CotacaoHistorica;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.repository.AcaoRepository;
import com.colavite.gestor_investimento.repository.CotacaoHistoricaRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CotacaoHistoricaControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired AcaoRepository acoes;
    @Autowired CotacaoHistoricaRepository historico;

    @Test
    void listaHistoricoEmOrdemCronologica() throws Exception {
        Acao acao = acoes.saveAndFlush(new Acao("PETR4", "Petrobras", Mercado.BRASIL, new BigDecimal("20"), Instant.parse("2026-09-02T12:00:00Z")));
        historico.save(new CotacaoHistorica(acao, new BigDecimal("21"), Instant.parse("2026-09-02T12:00:00Z"), Instant.parse("2026-09-02T12:01:00Z")));
        historico.save(new CotacaoHistorica(acao, new BigDecimal("19"), Instant.parse("2026-09-01T12:00:00Z"), Instant.parse("2026-09-01T12:01:00Z")));

        mockMvc.perform(get("/acoes/{id}/historico-cotacoes", acao.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cotacao").value(19))
                .andExpect(jsonPath("$[1].cotacao").value(21));
    }

    @Test
    void filtraHistoricoPorPeriodo() throws Exception {
        Acao acao = acoes.saveAndFlush(new Acao("AAPL", "Apple", Mercado.ESTADOS_UNIDOS, new BigDecimal("200"), Instant.parse("2026-09-02T12:00:00Z")));
        historico.save(new CotacaoHistorica(acao, new BigDecimal("200"), Instant.parse("2026-09-01T12:00:00Z"), Instant.now()));
        historico.save(new CotacaoHistorica(acao, new BigDecimal("210"), Instant.parse("2026-09-02T12:00:00Z"), Instant.now()));

        mockMvc.perform(get("/acoes/{id}/historico-cotacoes", acao.getId())
                        .param("de", "2026-09-02T00:00:00Z").param("ate", "2026-09-02T23:59:59Z"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].cotacao").value(210));
    }

    @Test
    void rejeitaIntervaloInvertidoEAcoesInexistentes() throws Exception {
        mockMvc.perform(get("/acoes/999999/historico-cotacoes"))
                .andExpect(status().isNotFound());
        Acao acao = acoes.saveAndFlush(new Acao("VALE3", "Vale", Mercado.BRASIL, new BigDecimal("60"), Instant.now()));
        mockMvc.perform(get("/acoes/{id}/historico-cotacoes", acao.getId())
                        .param("de", "2026-09-03T00:00:00Z").param("ate", "2026-09-02T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }
}
