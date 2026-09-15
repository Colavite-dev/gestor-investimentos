package com.colavite.gestor_investimento.mapper;

import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.support.TestUsuarios;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorretoraMapperTest {

    @Test
    void deveNormalizarDadosCadastraisEMapearResponseSemValidacaoCvm() {
        CnpjRegistrationData data = new CnpjRegistrationData(
                "11.222.333/0001-81",
                "  Corretora Exemplo S.A.  ",
                " ",
                null,
                " ",
                "01001-000",
                " Praça da Sé ",
                " 100 ",
                " ",
                " Sé ",
                " São Paulo ",
                "sp",
                " ATIVA "
        );

        Corretora entity = CorretoraMapper.toEntity(data, TestUsuarios.novo("broker-mapper"));
        CorretoraResponse response = CorretoraMapper.toResponse(entity);

        assertThat(response.cnpj()).isEqualTo("11222333000181");
        assertThat(response.cep()).isEqualTo("01001000");
        assertThat(response.uf()).isEqualTo("SP");
        assertThat(response.razaoSocial()).isEqualTo("Corretora Exemplo S.A.");
        assertThat(response.nomeFantasia()).isNull();
        assertThat(response.telefone()).isNull();
        assertThat(response.complemento()).isNull();
        assertThat(response.validadaNaCvm()).isFalse();
        assertThat(response.dataCadastro()).isNotNull();
    }
}
