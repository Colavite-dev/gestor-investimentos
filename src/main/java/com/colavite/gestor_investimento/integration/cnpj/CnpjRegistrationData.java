package com.colavite.gestor_investimento.integration.cnpj;

public record CnpjRegistrationData(
        String cnpj,
        String razaoSocial,
        String nomeFantasia,
        String email,
        String telefone,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String situacaoCadastral
) {
}
