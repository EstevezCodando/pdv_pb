package com.pdv.pontovenda.config;

import org.springframework.stereotype.Component;

/**
 * Valida identificadores de entrada antes de acessar a camada de negocio.
 */
@Component
public class ValidadorDeIdentificador {

    public void validarPositivo(Long id, String nomeCampo) {
        if (id == null) {
            throw new IllegalArgumentException(nomeCampo + " e obrigatorio");
        }
        if (id <= 0) {
            throw new IllegalArgumentException(nomeCampo + " deve ser um numero positivo");
        }
    }
}
