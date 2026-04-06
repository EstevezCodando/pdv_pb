package com.pdv.pontovenda.test;

import com.pdv.pontovenda.config.AutenticacaoListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Testes unitarios para AutenticacaoListener.
 * Verifica que os eventos de login sao tratados sem erros.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AutenticacaoListener")
class AutenticacaoListenerTest {

    @InjectMocks
    private AutenticacaoListener listener;

    @Test
    @DisplayName("Deve registrar login bem-sucedido sem lancas excecao")
    void deveRegistrarLoginSucesso() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin@pdv.com", null, List.of());
        AuthenticationSuccessEvent evento = new AuthenticationSuccessEvent(auth);

        assertDoesNotThrow(() -> listener.onLoginSucesso(evento));
    }

    @Test
    @DisplayName("Deve registrar falha de login sem lancar excecao")
    void deveRegistrarLoginFalha() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "hacker@pdv.com", "senha-errada");
        BadCredentialsException causa = new BadCredentialsException("Credenciais invalidas");

        AbstractAuthenticationFailureEvent evento = new AbstractAuthenticationFailureEvent(auth, causa) {};

        assertDoesNotThrow(() -> listener.onLoginFalha(evento));
    }
}
