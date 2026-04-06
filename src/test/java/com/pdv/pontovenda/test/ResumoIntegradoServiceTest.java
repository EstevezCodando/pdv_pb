package com.pdv.pontovenda.test;

import com.pdv.pontovenda.dto.ResumoIntegradoResponse;
import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.service.ProdutoService;
import com.pdv.pontovenda.service.ResumoIntegradoService;
import com.pdv.pontovenda.service.UsuarioService;
import com.pdv.pontovenda.service.VendaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResumoIntegradoService")
class ResumoIntegradoServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ProdutoService produtoService;

    @Mock
    private VendaService vendaService;

    @InjectMocks
    private ResumoIntegradoService resumoIntegradoService;

    @Test
    @DisplayName("Deve consolidar usuarios, produtos, estoque, faturamento e ticket medio")
    void deveGerarResumoConsolidado() {
        Produto arroz = new Produto(1L, "Arroz", "Tipo 1", new BigDecimal("10.00"), 5, "111", true);
        Produto feijao = new Produto(2L, "Feijao", "Carioca", new BigDecimal("7.50"), 15, "222", true);
        Produto inativo = new Produto(3L, "Cafe", "Extra forte", new BigDecimal("20.00"), 0, "333", false);

        when(usuarioService.contarTodos()).thenReturn(4L);
        when(usuarioService.contarAtivos()).thenReturn(3L);
        when(produtoService.contarTodos()).thenReturn(3L);
        when(produtoService.contarAtivos()).thenReturn(2L);
        when(produtoService.listarTodos()).thenReturn(List.of(arroz, feijao, inativo));
        when(vendaService.contarTodas()).thenReturn(2L);
        when(vendaService.calcularFaturamentoTotal()).thenReturn(new BigDecimal("300.00"));

        ResumoIntegradoResponse resumo = resumoIntegradoService.gerarResumo();

        assertEquals(4L, resumo.totalUsuarios());
        assertEquals(3L, resumo.totalUsuariosAtivos());
        assertEquals(3L, resumo.totalProdutos());
        assertEquals(2L, resumo.totalProdutosAtivos());
        assertEquals(20L, resumo.totalItensEmEstoque());
        assertEquals(new BigDecimal("162.50"), resumo.valorTotalEstoque());
        assertEquals(2L, resumo.totalProdutosComEstoqueBaixo());
        assertEquals(2L, resumo.totalVendas());
        assertEquals(new BigDecimal("300.00"), resumo.faturamentoTotal());
        assertEquals(new BigDecimal("150.00"), resumo.ticketMedio());
    }
}
