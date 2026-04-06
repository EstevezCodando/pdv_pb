package com.pdv.pontovenda.dto;

import java.math.BigDecimal;

/**
 * Projecao de saida de um item vendido.
 */
public record ItemVendaResponse(
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
}
