package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Carteira;
import com.colavite.gestor_investimento.entity.Mercado;
import com.colavite.gestor_investimento.entity.Operacao;
import com.colavite.gestor_investimento.entity.TipoOperacao;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OperacaoSaldoIntegridadeRepositoryTest {

    @Autowired private OperacaoRepository operacoes;
    @Autowired private CarteiraRepository carteiras;
    @Autowired private AcaoRepository acoes;
    @Autowired private UsuarioRepository usuarios;

    @Test
    void ordenaHistoricoPorDataEIdEAquireBloqueioDaCarteira() {
        var owner = TestUsuarios.persistir(usuarios, "saldo-repository");
        Carteira carteira = carteiras.saveAndFlush(new Carteira("Carteira ordenada", "CARTEIRA ORDENADA", null, owner));
        Acao acao = acoes.saveAndFlush(new Acao("PETR4", "Petrobras", Mercado.BRASIL, new BigDecimal("30"), Instant.now()));
        Operacao posterior = operacoes.saveAndFlush(new Operacao(carteira, acao, TipoOperacao.COMPRA, BigDecimal.ONE, BigDecimal.ONE, Instant.parse("2026-09-03T12:00:00Z")));
        Operacao anterior = operacoes.saveAndFlush(new Operacao(carteira, acao, TipoOperacao.COMPRA, BigDecimal.ONE, BigDecimal.ONE, Instant.parse("2026-09-01T12:00:00Z")));

        assertThat(carteiras.findByIdAndUsuarioIdForUpdate(carteira.getId(), owner.getId())).isPresent();
        assertThat(operacoes.findByCarteiraIdAndAcaoIdOrderByDataOperacaoAscIdAsc(carteira.getId(), acao.getId()))
                .extracting(Operacao::getId)
                .containsExactly(anterior.getId(), posterior.getId());
    }
}
