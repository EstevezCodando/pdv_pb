package com.pdv.pontovenda.dto;

import java.math.BigDecimal;

/**
 * Representa uma linha de produto no formulario de nova venda.
 * Usado para binding com Thymeleaf — requer getters e setters padrao.
 */
public class ItemVendaForm {

    private Long produtoId;
    private String nomeProduto;
    private BigDecimal preco;
    private Integer quantidade = 0;

    public ItemVendaForm() {}

    public ItemVendaForm(Long produtoId, String nomeProduto, BigDecimal preco) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.preco = preco;
    }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}
