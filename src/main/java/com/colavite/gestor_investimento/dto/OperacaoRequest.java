package com.colavite.gestor_investimento.dto;
import com.colavite.gestor_investimento.entity.TipoOperacao;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
public record OperacaoRequest(@NotNull(message="Carteira é obrigatória") @Positive Long carteiraId,
                              @NotNull(message="Ação é obrigatória") @Positive Long acaoId,
                              @NotNull(message="Tipo é obrigatório") TipoOperacao tipo,
                              @NotNull @DecimalMin(value="0.00000001", message="Quantidade deve ser positiva") @Digits(integer = 11, fraction = 8) BigDecimal quantidade,
                              @NotNull @DecimalMin(value="0.00000001", message="Preço deve ser positivo") @Digits(integer = 11, fraction = 8) BigDecimal precoUnitario,
                              @NotNull(message="Data da operação é obrigatória") Instant dataOperacao) {}
