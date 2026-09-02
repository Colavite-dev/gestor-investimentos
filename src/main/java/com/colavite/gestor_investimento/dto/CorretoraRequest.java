package com.colavite.gestor_investimento.dto;

import com.colavite.gestor_investimento.validation.ValidCnpj;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CorretoraRequest(
        @NotBlank(message = "CNPJ é obrigatório")
        @Size(max = 18, message = "CNPJ deve ter no máximo 18 caracteres")
        @ValidCnpj
        String cnpj
) {
}
