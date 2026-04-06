package com.pdv.pontovenda.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Item solicitado para registrar uma venda.
 */
public record ItemVendaRequest(
        @NotNull(message = "O produtoId e obrigatorio")
        Long produtoId,
        @NotNull(message = "A quantidade e obrigatoria")
        @Min(value = 1, message = "A quantidade deve ser maior que zero")
        Integer quantidade
) {
}
