package com.pdv.pontovenda.exception;

/**
 * Excecao lancada quando uma regra de negocio e violada.
 * Exemplos: e-mail duplicado, estoque insuficiente, dados inconsistentes.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
