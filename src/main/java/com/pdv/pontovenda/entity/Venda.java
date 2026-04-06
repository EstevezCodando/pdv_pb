package com.pdv.pontovenda.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidade agregadora que representa uma venda finalizada no PDV.
 */
@Entity
@Table(name = "venda")
@Getter
@NoArgsConstructor
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "quantidade_total_itens", nullable = false)
    private Integer quantidadeTotalItens;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ItemVenda> itens = new ArrayList<>();

    private Venda(Usuario usuario,
                  FormaPagamento formaPagamento,
                  LocalDateTime dataHora,
                  BigDecimal valorTotal,
                  Integer quantidadeTotalItens) {
        this.usuario = usuario;
        this.formaPagamento = formaPagamento;
        this.dataHora = dataHora;
        this.valorTotal = valorTotal;
        this.quantidadeTotalItens = quantidadeTotalItens;
    }

    public static Venda criar(Usuario usuario,
                              FormaPagamento formaPagamento,
                              LocalDateTime dataHora,
                              BigDecimal valorTotal,
                              Integer quantidadeTotalItens) {
        return new Venda(usuario, formaPagamento, dataHora, valorTotal, quantidadeTotalItens);
    }

    public void adicionarItem(ItemVenda itemVenda) {
        itemVenda.vincularVenda(this);
        this.itens.add(itemVenda);
    }

    public List<ItemVenda> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
