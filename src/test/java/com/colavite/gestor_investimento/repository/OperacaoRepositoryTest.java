package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.*;
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
class OperacaoRepositoryTest {
    @Autowired CarteiraRepository carteiras;
    @Autowired AcaoRepository acoes;
    @Autowired OperacaoRepository operacoes;
    @Autowired UsuarioRepository usuarios;

    @Test
    void persisteOperacaoComForeignKeysEV4() {
        var owner = TestUsuarios.persistir(usuarios, "operacao-repo");
        var carteira = carteiras.saveAndFlush(new Carteira("Teste", "TESTE", null, owner));
        var acao = acoes.saveAndFlush(new Acao("PETR4", "Petrobras", Mercado.BRASIL, new BigDecimal("30"), Instant.now()));
        var operacao = operacoes.saveAndFlush(new Operacao(carteira, acao, TipoOperacao.COMPRA, BigDecimal.TEN,
                new BigDecimal("30"), Instant.parse("2026-09-01T12:00:00Z")));
        assertThat(operacoes.findById(operacao.getId())).isPresent();
        assertThat(operacoes.findByCarteiraIdOrderByDataOperacaoAscIdAsc(carteira.getId())).hasSize(1);
        assertThat(operacoes.findByIdAndCarteiraUsuarioId(operacao.getId(), owner.getId())).contains(operacao);
        var otherOwner = TestUsuarios.persistir(usuarios, "operacao-repo-other");
        assertThat(operacoes.findByIdAndCarteiraUsuarioId(operacao.getId(), otherOwner.getId())).isEmpty();
    }
}
