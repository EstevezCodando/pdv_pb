package com.pdv.pontovenda.dto;

import com.pdv.pontovenda.entity.FormaPagamento;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de formulario para registro de nova venda via interface web.
 * Thymeleaf faz o binding com a lista de itens indexada.
 */
public class NovaVendaForm {

    private Long usuarioId;
    private FormaPagamento formaPagamento;
    private List<ItemVendaForm> itens = new ArrayList<>();

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public List<ItemVendaForm> getItens() { return itens; }
    public void setItens(List<ItemVendaForm> itens) { this.itens = itens; }
}
