package com.pdv.pontovenda.dto;

import com.pdv.pontovenda.entity.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Contrato de entrada para fechamento de venda.
 */
public record CriarVendaRequest(
        @NotNull(message = "O usuarioId e obrigatorio")
        Long usuarioId,
        @NotNull(message = "A forma de pagamento e obrigatoria")
        FormaPagamento formaPagamento,
        @NotEmpty(message = "A venda deve possuir ao menos um item")
        List<@Valid ItemVendaRequest> itens
) {
}
