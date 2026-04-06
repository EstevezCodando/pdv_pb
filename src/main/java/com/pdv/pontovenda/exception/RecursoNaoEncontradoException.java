package com.pdv.pontovenda.exception;

/**
 * Excecao lancada quando um recurso solicitado nao e encontrado no banco de dados.
 * Utilizada pelos services para sinalizar falhas de busca de forma explicita.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String entidade, Long id) {
        super(String.format("%s com ID %d nao encontrado(a)", entidade, id));
    }
}
