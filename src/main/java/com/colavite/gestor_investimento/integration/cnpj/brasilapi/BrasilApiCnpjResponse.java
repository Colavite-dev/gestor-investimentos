package com.colavite.gestor_investimento.integration.cnpj.brasilapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BrasilApiCnpjResponse(
        String cnpj,
        @JsonProperty("razao_social") String razaoSocial,
        @JsonProperty("nome_fantasia") String nomeFantasia,
        String email,
        @JsonProperty("ddd_telefone_1") String telefone,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String municipio,
        String uf,
        @JsonProperty("descricao_situacao_cadastral") String situacaoCadastral
) {
}
