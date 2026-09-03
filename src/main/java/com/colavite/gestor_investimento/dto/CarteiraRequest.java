package com.colavite.gestor_investimento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CarteiraRequest(@NotBlank(message = "Nome é obrigatório") @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres") String nome,
                              @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres") String descricao) {}
