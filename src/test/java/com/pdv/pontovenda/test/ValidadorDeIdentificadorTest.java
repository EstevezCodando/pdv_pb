package com.pdv.pontovenda.test;

import com.pdv.pontovenda.config.ValidadorDeIdentificador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para ValidadorDeIdentificador.
 */
@DisplayName("ValidadorDeIdentificador")
class ValidadorDeIdentificadorTest {

    private final ValidadorDeIdentificador validador = new ValidadorDeIdentificador();

    @Test
    @DisplayName("Deve aceitar ID positivo valido sem lancar excecao")
    void deveAceitarIdPositivo() {
        assertDoesNotThrow(() -> validador.validarPositivo(1L, "id"));
        assertDoesNotThrow(() -> validador.validarPositivo(999L, "id"));
    }

    @Test
    @DisplayName("Deve lancar excecao para ID nulo")
    void deveLancarExcecaoParaIdNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validarPositivo(null, "identificador"));
        assertTrue(ex.getMessage().contains("identificador"));
    }

    @Test
    @DisplayName("Deve lancar excecao para ID zero")
    void deveLancarExcecaoParaIdZero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validarPositivo(0L, "id"));
        assertTrue(ex.getMessage().contains("positivo"));
    }

    @Test
    @DisplayName("Deve lancar excecao para ID negativo")
    void deveLancarExcecaoParaIdNegativo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validarPositivo(-5L, "campo"));
        assertTrue(ex.getMessage().contains("positivo"));
    }
}
