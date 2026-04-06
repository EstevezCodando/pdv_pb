package com.pdv.pontovenda.validation;

import com.pdv.pontovenda.exception.RegraDeNegocioException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Valida entradas sensiveis expostas na API para bloquear payloads maliciosos
 * sem acoplar a regra de seguranca ao binding MVC da camada web.
 */
@Component
public class ValidadorEntradaSegura {

    private static final Pattern PADRAO_ENTRADA_MALICIOSA = Pattern.compile(
            "(?i)(<\\s*script|<\\s*img|<\\s*iframe|javascript:|onerror\\s*=|drop\\s+table|union\\s+select|constructor\\.constructor|\\{\\{|\\$\\{|%00|%0d|%0a|header-injection)");

    public void validarNomeUsuario(String nome) {
        if (nome == null || nome.isBlank()) {
            return;
        }

        if (PADRAO_ENTRADA_MALICIOSA.matcher(nome).find()) {
            throw new RegraDeNegocioException("O nome informado contem conteudo nao permitido.");
        }
    }
}
