package com.pdv.pontovenda.dto;

import com.pdv.pontovenda.entity.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resposta consolidada do fechamento de venda.
 */
public record ResumoVendaResponse(
        Long vendaId,
        Long usuarioId,
        String nomeUsuario,
        FormaPagamento formaPagamento,
        LocalDateTime dataHora,
        Integer quantidadeTotalItens,
        BigDecimal valorTotal,
        List<ItemVendaResponse> itens
) {
}
