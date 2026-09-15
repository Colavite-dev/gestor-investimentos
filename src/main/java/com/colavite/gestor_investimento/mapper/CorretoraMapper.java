package com.colavite.gestor_investimento.mapper;

import com.colavite.gestor_investimento.dto.CorretoraResponse;
import com.colavite.gestor_investimento.entity.Corretora;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.integration.cnpj.CnpjRegistrationData;
import com.colavite.gestor_investimento.validation.CnpjUtils;

import java.util.Locale;

public final class CorretoraMapper {

    private CorretoraMapper() {
    }

    public static Corretora toEntity(CnpjRegistrationData data, Usuario usuario) {
        return new Corretora(
                CnpjUtils.somenteDigitos(data.cnpj()),
                data.razaoSocial().trim(),
                optional(data.nomeFantasia()),
                optional(data.email()),
                optional(data.telefone()),
                CnpjUtils.somenteDigitos(data.cep()),
                data.logradouro().trim(),
                data.numero().trim(),
                optional(data.complemento()),
                data.bairro().trim(),
                data.cidade().trim(),
                data.uf().trim().toUpperCase(Locale.ROOT),
                data.situacaoCadastral().trim(),
                usuario
        );
    }

    public static CorretoraResponse toResponse(Corretora corretora) {
        return new CorretoraResponse(
                corretora.getId(),
                corretora.getCnpj(),
                corretora.getRazaoSocial(),
                corretora.getNomeFantasia(),
                corretora.getEmail(),
                corretora.getTelefone(),
                corretora.getCep(),
                corretora.getLogradouro(),
                corretora.getNumero(),
                corretora.getComplemento(),
                corretora.getBairro(),
                corretora.getCidade(),
                corretora.getUf(),
                corretora.getSituacaoCadastral(),
                corretora.isValidadaNaCvm(),
                corretora.getDataCadastro()
        );
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
