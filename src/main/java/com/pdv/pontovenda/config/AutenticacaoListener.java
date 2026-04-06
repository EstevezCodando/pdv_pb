package com.pdv.pontovenda.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Registra eventos de autenticacao para rastreabilidade de acesso ao sistema.
 */
@Component
public class AutenticacaoListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutenticacaoListener.class);

    @EventListener
    public void onLoginSucesso(AuthenticationSuccessEvent evento) {
        String email = evento.getAuthentication().getName();
        LOGGER.info("login-sucesso email={}", email);
    }

    @EventListener
    public void onLoginFalha(AbstractAuthenticationFailureEvent evento) {
        String email = evento.getAuthentication().getName();
        String motivo = evento.getException().getClass().getSimpleName();
        LOGGER.warn("login-falha email={} motivo={}", email, motivo);
    }
}
