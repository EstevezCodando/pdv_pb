package com.pdv.pontovenda.test;

import com.pdv.pontovenda.dto.CriarVendaRequest;
import com.pdv.pontovenda.dto.ItemVendaRequest;
import com.pdv.pontovenda.dto.ResumoVendaResponse;
import com.pdv.pontovenda.entity.FormaPagamento;
import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.entity.Venda;
import com.pdv.pontovenda.exception.EstoqueInsuficienteException;
import com.pdv.pontovenda.repository.VendaRepository;
import com.pdv.pontovenda.service.ProdutoService;
import com.pdv.pontovenda.service.UsuarioService;
import com.pdv.pontovenda.service.VendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendaService")
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private VendaService vendaService;

    private Usuario usuario;
    private Produto arroz;
    private Produto feijao;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(1L, "Operador", "operador@pdv.com", "senha123", "OPERADOR", true);
        arroz = new Produto(1L, "Arroz", "Integral", new BigDecimal("10.00"), 10, "111", true);
        feijao = new Produto(2L, "Feijao", "Preto", new BigDecimal("8.50"), 20, "222", true);
    }

    @Test
    @DisplayName("Deve registrar venda e baixar estoque")
    void deveRegistrarVendaComSucesso() {
        CriarVendaRequest requisicao = new CriarVendaRequest(
                1L,
                FormaPagamento.PIX,
                List.of(new ItemVendaRequest(1L, 2), new ItemVendaRequest(2L, 1))
        );

        when(usuarioService.buscarAtivoPorId(1L)).thenReturn(usuario);
        when(produtoService.buscarAtivoPorId(1L)).thenReturn(arroz);
        when(produtoService.buscarAtivoPorId(2L)).thenReturn(feijao);
        doNothing().when(produtoService).baixarEstoque(any(Produto.class), any(Integer.class));
        when(vendaRepository.save(any(Venda.class))).thenAnswer(invocacao -> {
            Venda venda = invocacao.getArgument(0);
            return venda;
        });

        ResumoVendaResponse resposta = vendaService.registrarVenda(requisicao);

        verify(produtoService).baixarEstoque(arroz, 2);
        verify(produtoService).baixarEstoque(feijao, 1);
        verify(vendaRepository, times(1)).save(any(Venda.class));
        assertEquals(3, resposta.quantidadeTotalItens());
        assertEquals(new BigDecimal("28.50"), resposta.valorTotal());
        assertEquals(2, resposta.itens().size());
    }

    @Test
    @DisplayName("Deve impedir venda quando o estoque e insuficiente")
    void deveFalharQuandoEstoqueForInsuficiente() {
        arroz.setQuantidadeEstoque(1);
        CriarVendaRequest requisicao = new CriarVendaRequest(
                1L,
                FormaPagamento.DINHEIRO,
                List.of(new ItemVendaRequest(1L, 2))
        );

        when(usuarioService.buscarAtivoPorId(1L)).thenReturn(usuario);
        when(produtoService.buscarAtivoPorId(1L)).thenReturn(arroz);

        assertThrows(EstoqueInsuficienteException.class, () -> vendaService.registrarVenda(requisicao));
    }
}
