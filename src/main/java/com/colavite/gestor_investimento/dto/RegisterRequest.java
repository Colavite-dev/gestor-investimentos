package com.colavite.gestor_investimento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 120) String nome,
        @NotBlank(message = "Username é obrigatório") @Size(max = 60) String username,
        @NotBlank(message = "Email é obrigatório") @Size(max = 255) String email,
        @NotBlank(message = "Senha é obrigatória") @Size(max = 200) String password
) {
}
