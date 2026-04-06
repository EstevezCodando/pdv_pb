package com.pdv.pontovenda.dto;

import java.math.BigDecimal;

/**
 * Representa uma visao consolidada dos modulos integrados do sistema.
 */
public record ResumoIntegradoResponse(
        long totalUsuarios,
        long totalUsuariosAtivos,
        long totalProdutos,
        long totalProdutosAtivos,
        long totalItensEmEstoque,
        BigDecimal valorTotalEstoque,
        long totalProdutosComEstoqueBaixo,
        long totalVendas,
        BigDecimal faturamentoTotal,
        BigDecimal ticketMedio
) {
}
