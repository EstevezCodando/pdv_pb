package com.pdv.pontovenda.service;

import com.pdv.pontovenda.dto.CriarVendaRequest;
import com.pdv.pontovenda.dto.ItemVendaRequest;
import com.pdv.pontovenda.dto.ItemVendaResponse;
import com.pdv.pontovenda.dto.ResumoVendaResponse;
import com.pdv.pontovenda.entity.ItemVenda;
import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.entity.Venda;
import com.pdv.pontovenda.exception.EstoqueInsuficienteException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import com.pdv.pontovenda.repository.VendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Orquestra o fluxo transacional de fechamento de venda, integrando usuarios e produtos.
 */
@Service
public class VendaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendaService.class);

    private final VendaRepository vendaRepository;
    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;

    public VendaService(VendaRepository vendaRepository,
                        UsuarioService usuarioService,
                        ProdutoService produtoService) {
        this.vendaRepository = vendaRepository;
        this.usuarioService = usuarioService;
        this.produtoService = produtoService;
    }

    @Transactional(readOnly = true)
    public List<ResumoVendaResponse> listarTodas() {
        return vendaRepository.findAll().stream()
                .map(this::mapearResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarTodas() {
        return vendaRepository.count();
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularFaturamentoTotal() {
        return vendaRepository.somarValorTotal();
    }

    @Transactional
    public ResumoVendaResponse registrarVenda(CriarVendaRequest requisicao) {
        Usuario usuario = usuarioService.buscarAtivoPorId(requisicao.usuarioId());

        ValidacaoVenda acumulador = new ValidacaoVenda();
        List<ItemVenda> itensVenda = requisicao.itens().stream()
                .map(item -> criarItemVenda(item, acumulador))
                .toList();

        Venda venda = Venda.criar(
                usuario,
                requisicao.formaPagamento(),
                LocalDateTime.now(),
                acumulador.valorTotal(),
                acumulador.quantidadeTotalItens()
        );
        itensVenda.forEach(venda::adicionarItem);

        ResumoVendaResponse resumo = mapearResumo(vendaRepository.save(venda));
        LOGGER.info("venda-concluida id={} operador={} total={} itens={}",
                resumo.vendaId(), usuario.getNome(), acumulador.valorTotal(), acumulador.quantidadeTotalItens());
        return resumo;
    }

    private ItemVenda criarItemVenda(ItemVendaRequest item, ValidacaoVenda acumulador) {
        Produto produto = produtoService.buscarAtivoPorId(item.produtoId());
        validarEstoque(produto, item.quantidade());
        produtoService.baixarEstoque(produto, item.quantidade());

        BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.quantidade()));
        acumulador.somar(item.quantidade(), subtotal);
        return ItemVenda.criar(produto, item.quantidade(), produto.getPreco(), subtotal);
    }

    private void validarEstoque(Produto produto, Integer quantidadeSolicitada) {
        Integer quantidadeDisponivel = produto.getQuantidadeEstoque();
        if (quantidadeDisponivel == null || quantidadeDisponivel < quantidadeSolicitada) {
            throw new EstoqueInsuficienteException(produto.getNome(), quantidadeDisponivel == null ? 0 : quantidadeDisponivel,
                    quantidadeSolicitada);
        }
    }

    private ResumoVendaResponse mapearResumo(Venda venda) {
        return new ResumoVendaResponse(
                venda.getId(),
                venda.getUsuario().getId(),
                venda.getUsuario().getNome(),
                venda.getFormaPagamento(),
                venda.getDataHora(),
                venda.getQuantidadeTotalItens(),
                venda.getValorTotal(),
                venda.getItens().stream()
                        .map(item -> new ItemVendaResponse(
                                item.getProduto().getId(),
                                item.getProduto().getNome(),
                                item.getQuantidade(),
                                item.getPrecoUnitario(),
                                item.getSubtotal()))
                        .toList()
        );
    }

    private static final class ValidacaoVenda {
        private int quantidadeTotalItens;
        private BigDecimal valorTotal = BigDecimal.ZERO;

        void somar(Integer quantidade, BigDecimal subtotal) {
            this.quantidadeTotalItens += quantidade;
            this.valorTotal = this.valorTotal.add(subtotal);
        }

        Integer quantidadeTotalItens() {
            return quantidadeTotalItens;
        }

        BigDecimal valorTotal() {
            return valorTotal;
        }
    }
}
