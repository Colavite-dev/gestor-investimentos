package com.colavite.gestor_investimento.repository;

import com.colavite.gestor_investimento.entity.Acao;
import com.colavite.gestor_investimento.entity.Mercado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class FinancialNumericSchemaIntegrationTest {

    @Autowired private AcaoRepository acoes;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void flywayV6AplicaPrecisaoOitoEConstraintPositivaNoH2() {
        Integer escala = jdbc.queryForObject("SELECT NUMERIC_SCALE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'ACOES' AND COLUMN_NAME = 'COTACAO_ATUAL'", Integer.class);
        assertThat(escala).isEqualTo(8);

        Acao oitoCasas = acoes.saveAndFlush(new Acao("PREC8H2", "Precisao", Mercado.BRASIL,
                new BigDecimal("99999999999.12345678"), Instant.parse("2026-09-01T12:00:00Z")));
        assertThat(acoes.findById(oitoCasas.getId()).orElseThrow().getCotacaoAtual())
                .isEqualByComparingTo("99999999999.12345678");

        assertThatThrownBy(() -> acoes.saveAndFlush(new Acao("ZEROH2", "Zero", Mercado.BRASIL,
                BigDecimal.ZERO, Instant.parse("2026-09-01T12:00:00Z"))))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> acoes.saveAndFlush(new Acao("NEGH2", "Negativa", Mercado.BRASIL,
                new BigDecimal("-0.01"), Instant.parse("2026-09-01T12:00:00Z"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
