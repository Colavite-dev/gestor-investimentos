package com.colavite.gestor_investimento.integration.cvm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CvmParticipantEligibilityTest {

    private static final String CNPJ = "02332886000104";

    @ParameterizedTest
    @ValueSource(strings = {
            "CORRETORAS",
            "DISTRIBUIDORAS",
            "CORRETORA DE TÍTULOS E VALORES MOBILIÁRIOS",
            "DISTRIBUIDORA DE TÍTULOS E VALORES MOBILIÁRIOS"
    })
    void aceitaSomenteCategoriaCompativelEmFuncionamentoNormal(String categoria) {
        CvmParticipantData participant = new CvmParticipantData(
                CNPJ, "EM FUNCIONAMENTO NORMAL", categoria);

        assertThat(CvmParticipantEligibility.isEligible(participant)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "ATIVO",
            "CANCELADA",
            "LIQUIDAÇÃO EXTRAJUDICIAL",
            "SUSPENSA",
            "DESCONHECIDA"
    })
    void rejeitaSituacaoQueNaoRepresentaFuncionamentoNormal(String situacao) {
        CvmParticipantData participant = new CvmParticipantData(CNPJ, situacao, "CORRETORAS");

        assertThat(CvmParticipantEligibility.isEligible(participant)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "BANCOS COMERCIAIS",
            "CUSTODIANTES DE VALORES MOBILIÁRIOS",
            "ESCRITURADORES DE VALORES MOBILIÁRIOS"
    })
    void rejeitaCategoriaIncompativelMesmoEmFuncionamentoNormal(String categoria) {
        CvmParticipantData participant = new CvmParticipantData(
                CNPJ, "EM FUNCIONAMENTO NORMAL", categoria);

        assertThat(CvmParticipantEligibility.isEligible(participant)).isFalse();
    }

    @Test
    void rejeitaParticipanteAusente() {
        assertThat(CvmParticipantEligibility.isEligible(null)).isFalse();
    }
}
