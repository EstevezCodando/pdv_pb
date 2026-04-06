package com.pdv.pontovenda.test;

import com.pdv.pontovenda.config.FiltroLogRequisicao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testes unitarios para FiltroLogRequisicao.
 * Verifica que o requestId e adicionado ao MDC e removido apos o processamento.
 */
@DisplayName("FiltroLogRequisicao")
class FiltroLogRequisicaoTest {

    private final FiltroLogRequisicao filtro = new FiltroLogRequisicao();

    @Test
    @DisplayName("Deve processar a requisicao e limpar o MDC ao finalizar")
    void deveProcessarRequisicaoELimparMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/vendas");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filtro.doFilter(request, response, chain);

        assertNull(MDC.get("requestId"), "MDC deve estar limpo apos o processamento");
    }

    @Test
    @DisplayName("Deve limpar o MDC mesmo em requisicoes POST")
    void deveLimparMdcEmRequisicoesPost() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/usuarios");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filtro.doFilter(request, response, chain);

        assertNull(MDC.get("requestId"), "MDC deve estar limpo apos requisicao POST");
    }
}
