package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AcaoRepositoryTest {
    @Autowired AcaoRepository repository;

    @Test void persisteIdentidadeCompostaEPermiteMesmoTickerEmMercadosDiferentes() {
        Acao brasil = repository.saveAndFlush(entity("PETR4", Mercado.BRASIL));
        Acao eua = repository.saveAndFlush(entity("PETR4", Mercado.ESTADOS_UNIDOS));
        assertThat(repository.findByTickerAndMercado("PETR4", Mercado.BRASIL)).contains(brasil);
        assertThat(repository.findByTicker("PETR4")).containsExactlyInAnyOrder(brasil, eua);
    }

    @Test void bancoGaranteUnicidadeTickerEMercado() {
        repository.saveAndFlush(entity("AAPL", Mercado.ESTADOS_UNIDOS));
        assertThatThrownBy(() -> repository.saveAndFlush(entity("AAPL", Mercado.ESTADOS_UNIDOS)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Acao entity(String ticker, Mercado mercado) {
        return new Acao(ticker, "Empresa", mercado, new BigDecimal("10.5000"), Instant.parse("2026-09-02T12:00:00Z"));
    }
}
