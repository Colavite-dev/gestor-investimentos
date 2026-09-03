package com.colavite.gestor_investimento.service;
import com.colavite.gestor_investimento.dto.OperacaoRequest;
import com.colavite.gestor_investimento.entity.*;
import com.colavite.gestor_investimento.repository.*;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import org.mockito.Mock; import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal; import java.time.Instant; import java.util.Optional;
import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.any; import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class OperacaoServiceTest {
 @Mock OperacaoRepository operacoes; @Mock CarteiraRepository carteiras; @Mock AcaoRepository acoes;
 @Test void cadastraCompraComReferenciasExistentes() { var carteira=new Carteira("Carteira","CARTEIRA",null); var acao=new Acao("PETR4","Petrobras",Mercado.BRASIL,new BigDecimal("30"),Instant.now()); when(carteiras.findById(1L)).thenReturn(Optional.of(carteira)); when(acoes.findById(2L)).thenReturn(Optional.of(acao)); when(operacoes.saveAndFlush(any())).thenAnswer(i->i.getArgument(0)); var response=new OperacaoService(operacoes,carteiras,acoes).cadastrar(new OperacaoRequest(1L,2L,TipoOperacao.COMPRA,new BigDecimal("10"),new BigDecimal("30"),Instant.parse("2026-09-01T12:00:00Z"))); assertThat(response.tipo()).isEqualTo(TipoOperacao.COMPRA); verify(operacoes).saveAndFlush(any()); }
 @Test void naoPersisteQuandoCarteiraNaoExiste() { when(carteiras.findById(9L)).thenReturn(Optional.empty()); assertThatThrownBy(()->new OperacaoService(operacoes,carteiras,acoes).cadastrar(new OperacaoRequest(9L,2L,TipoOperacao.COMPRA,BigDecimal.ONE,BigDecimal.ONE,Instant.now()))).isInstanceOf(com.colavite.gestor_investimento.exception.CarteiraNotFoundException.class); verifyNoInteractions(acoes,operacoes); }
}
