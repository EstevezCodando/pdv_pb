package com.pdv.pontovenda.service;

import com.pdv.pontovenda.dto.ResumoIntegradoResponse;
import com.pdv.pontovenda.entity.Produto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Orquestra a integracao entre os modulos de usuario, produto e vendas, oferecendo uma visao consolidada.
 */
@Service
public class ResumoIntegradoService {

    private static final int LIMIAR_ESTOQUE_BAIXO = 10;

    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;
    private final VendaService vendaService;

    public ResumoIntegradoService(UsuarioService usuarioService,
                                  ProdutoService produtoService,
                                  VendaService vendaService) {
        this.usuarioService = usuarioService;
        this.produtoService = produtoService;
        this.vendaService = vendaService;
    }

    @Transactional(readOnly = true)
    public ResumoIntegradoResponse gerarResumo() {
        List<Produto> produtos = produtoService.listarTodos();
        long totalVendas = vendaService.contarTodas();
        BigDecimal faturamentoTotal = vendaService.calcularFaturamentoTotal();

        long totalItensEmEstoque = produtos.stream()
                .map(Produto::getQuantidadeEstoque)
                .filter(quantidade -> quantidade != null && quantidade > 0)
                .mapToLong(Integer::longValue)
                .sum();

        BigDecimal valorTotalEstoque = produtos.stream()
                .filter(produto -> produto.getPreco() != null && produto.getQuantidadeEstoque() != null)
                .map(produto -> produto.getPreco().multiply(BigDecimal.valueOf(produto.getQuantidadeEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalProdutosComEstoqueBaixo = produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() != null && p.getQuantidadeEstoque() <= LIMIAR_ESTOQUE_BAIXO)
                .count();

        BigDecimal ticketMedio = totalVendas == 0
                ? BigDecimal.ZERO
                : faturamentoTotal.divide(BigDecimal.valueOf(totalVendas), 2, RoundingMode.HALF_UP);

        return new ResumoIntegradoResponse(
                usuarioService.contarTodos(),
                usuarioService.contarAtivos(),
                produtoService.contarTodos(),
                produtoService.contarAtivos(),
                totalItensEmEstoque,
                valorTotalEstoque,
                totalProdutosComEstoqueBaixo,
                totalVendas,
                faturamentoTotal,
                ticketMedio
        );
    }
}
