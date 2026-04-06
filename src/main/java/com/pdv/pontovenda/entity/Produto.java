package com.pdv.pontovenda.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidade que representa um produto disponivel para venda no PDV.
 * Utiliza BigDecimal para valores monetarios, evitando problemas de precisao com double.
 */
@Entity
@Table(name = "produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto e obrigatorio")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nome;

    @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
    @Column(length = 500)
    private String descricao;

    @NotNull(message = "O preco e obrigatorio")
    @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Formato de preco invalido")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @NotNull(message = "A quantidade em estoque e obrigatoria")
    @Min(value = 0, message = "A quantidade nao pode ser negativa")
    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque;

    @Size(max = 50, message = "O codigo de barras deve ter no maximo 50 caracteres")
    @Column(name = "codigo_barras", length = 50, unique = true)
    private String codigoBarras;

    @Column(nullable = false)
    private Boolean ativo = true;
}
