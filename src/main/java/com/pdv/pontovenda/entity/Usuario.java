package com.pdv.pontovenda.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um usuario do sistema PDV.
 * Cada usuario possui um perfil (ADMIN ou OPERADOR) que define suas permissoes.
 */
@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome e obrigatorio")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "A senha e obrigatoria")
    @Size(min = 4, message = "A senha deve ter no minimo 4 caracteres")
    @Column(nullable = false, length = 255)
    private String senha;

    @NotBlank(message = "O perfil e obrigatorio")
    @Column(nullable = false, length = 20)
    private String perfil;

    @Column(nullable = false)
    private Boolean ativo = true;
}
