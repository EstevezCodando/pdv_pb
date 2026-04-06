package com.pdv.pontovenda.test;

import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios para as classes de excecao customizadas.
 * Garantem que as mensagens de erro sao formatadas corretamente.
 */
@DisplayName("Excecoes Customizadas")
class ExceptionTest {

    @Test
    @DisplayName("RecursoNaoEncontradoException deve formatar mensagem com entidade e ID")
    void deveFormatarMensagemComEntidadeEId() {
        RecursoNaoEncontradoException ex = new RecursoNaoEncontradoException("Usuario", 42L);
        assertThat(ex.getMessage()).isEqualTo("Usuario com ID 42 nao encontrado(a)");
    }

    @Test
    @DisplayName("RecursoNaoEncontradoException deve aceitar mensagem customizada")
    void deveAceitarMensagemCustomizada() {
        RecursoNaoEncontradoException ex = new RecursoNaoEncontradoException("Registro nao localizado");
        assertThat(ex.getMessage()).isEqualTo("Registro nao localizado");
    }

    @Test
    @DisplayName("RegraDeNegocioException deve conter mensagem descritiva")
    void deveConterMensagemDescritiva() {
        RegraDeNegocioException ex = new RegraDeNegocioException("E-mail ja cadastrado");
        assertThat(ex.getMessage()).isEqualTo("E-mail ja cadastrado");
    }
}
