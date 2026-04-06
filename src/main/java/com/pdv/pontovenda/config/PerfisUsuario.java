package com.pdv.pontovenda.config;

import java.util.List;

/**
 * Centraliza os perfis aceitos pela aplicacao para evitar duplicacao e divergencia.
 */
public final class PerfisUsuario {

    public static final String ADMIN = "ADMIN";
    public static final String OPERADOR = "OPERADOR";
    public static final List<String> DISPONIVEIS = List.of(ADMIN, OPERADOR);

    private PerfisUsuario() {
        throw new IllegalStateException("Classe utilitaria nao deve ser instanciada");
    }
}
