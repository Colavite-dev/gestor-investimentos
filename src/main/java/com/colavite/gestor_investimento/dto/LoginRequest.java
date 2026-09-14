package com.colavite.gestor_investimento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username é obrigatório") @Size(max = 60) String username,
        @NotBlank(message = "Senha é obrigatória") @Size(max = 200) String password
) {
}
