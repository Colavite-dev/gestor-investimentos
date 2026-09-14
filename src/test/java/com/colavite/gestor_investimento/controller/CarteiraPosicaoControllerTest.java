package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.entity.*;
import com.colavite.gestor_investimento.repository.*;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class CarteiraPosicaoControllerTest {
 @Autowired MockMvc mockMvc; @Autowired CarteiraRepository carteiras; @Autowired AcaoRepository acoes; @Autowired OperacaoRepository operacoes; @Autowired UsuarioRepository usuarios;
 @Test void consultaPosicoesEResumo() throws Exception { var owner=TestUsuarios.persistir(usuarios,"posicao-controller"); var c=carteiras.saveAndFlush(new Carteira("Resumo","RESUMO",null,owner)); var a=acoes.saveAndFlush(new Acao("PETR4","Petrobras",Mercado.BRASIL,new BigDecimal("12"),Instant.now())); operacoes.saveAndFlush(new Operacao(c,a,TipoOperacao.COMPRA,new BigDecimal("10"),new BigDecimal("10"),Instant.now())); mockMvc.perform(get("/carteiras/"+c.getId()+"/posicoes").with(user(owner.getId().toString()))).andExpect(status().isOk()).andExpect(jsonPath("$[0].quantidade").value(10)).andExpect(jsonPath("$[0].cotacaoAtual").value(12)).andExpect(jsonPath("$[0].rentabilidadePercentual").value(20)); mockMvc.perform(get("/carteiras/"+c.getId()+"/resumo").with(user(owner.getId().toString()))).andExpect(status().isOk()).andExpect(jsonPath("$.porMoeda.BRL.patrimonioAtual").value(120)).andExpect(jsonPath("$.porMoeda.BRL.rentabilidadePercentual").value(20)); }
 @Test void carteiraInexistenteRetorna404() throws Exception { var owner=TestUsuarios.persistir(usuarios,"posicao-not-found"); mockMvc.perform(get("/carteiras/999999/posicoes").with(user(owner.getId().toString()))).andExpect(status().isNotFound()); }
}
