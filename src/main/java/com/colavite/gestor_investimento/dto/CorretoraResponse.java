package com.colavite.gestor_investimento.dto;

import java.time.Instant;

public record CorretoraResponse(
        Long id,
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
        String situacaoCadastral,
        boolean validadaNaCvm,
        Instant dataCadastro
) {
}
