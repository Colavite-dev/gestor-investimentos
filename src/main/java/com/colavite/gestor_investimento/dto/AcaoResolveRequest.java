package com.colavite.gestor_investimento.dto;

import com.colavite.gestor_investimento.entity.Mercado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AcaoResolveRequest(
        @NotBlank(message = "Ticker é obrigatório")
        @Size(max = 20, message = "Ticker deve ter no máximo 20 caracteres")
        @Pattern(regexp = "\\s*[A-Za-z0-9.-]+\\s*", message = "Ticker contém caracteres inválidos") String ticker,
        @NotNull(message = "Mercado é obrigatório") Mercado mercado
) { }
