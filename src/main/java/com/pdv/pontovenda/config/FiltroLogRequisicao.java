package com.pdv.pontovenda.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adiciona correlacao de requisoes e logs estruturados para depuracao do ambiente.
 */
@Component
public class FiltroLogRequisicao extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiltroLogRequisicao.class);
    private static final String CHAVE_REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        MDC.put(CHAVE_REQUEST_ID, UUID.randomUUID().toString());
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracao = System.currentTimeMillis() - inicio;
            LOGGER.info("requisicao processada metodo={} uri={} status={} duracaoMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duracao);
            MDC.remove(CHAVE_REQUEST_ID);
        }
    }
}
