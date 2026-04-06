package com.pdv.pontovenda.exception;

/**
 * Erro de negocio especifico do fluxo de vendas.
 */
public class EstoqueInsuficienteException extends RegraDeNegocioException {

    public EstoqueInsuficienteException(String nomeProduto, Integer quantidadeDisponivel, Integer quantidadeSolicitada) {
        super("Estoque insuficiente para o produto '%s'. Disponivel: %d. Solicitado: %d."
                .formatted(nomeProduto, quantidadeDisponivel, quantidadeSolicitada));
    }
}
