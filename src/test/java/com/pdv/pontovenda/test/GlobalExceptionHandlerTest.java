package com.pdv.pontovenda.test;

import com.pdv.pontovenda.exception.GlobalExceptionHandler;
import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios para o GlobalExceptionHandler.
 * Cobre todas as ramificações: requisicoes API (JSON) e MVC (HTML)
 * para cada tipo de excecao.
 */
@DisplayName("GlobalExceptionHandler — Cobertura de Todas as Ramificacoes")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("RecursoNaoEncontradoException")
    class RecursoNaoEncontrado {

        @Test
        @DisplayName("Deve retornar JSON 404 para requisicao API")
        @SuppressWarnings("unchecked")
        void deveRetornarJson404ParaApi() {
            HttpServletRequest request = criarRequestApi("/api/usuarios/1");
            RecursoNaoEncontradoException ex = new RecursoNaoEncontradoException("Usuario", 1L);

            Object resultado = handler.handleRecursoNaoEncontrado(ex, request);

            assertThat(resultado).isInstanceOf(ResponseEntity.class);
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) resultado;
            assertThat(response.getStatusCode().value()).isEqualTo(404);
        }

        @Test
        @DisplayName("Deve retornar ModelAndView para requisicao MVC")
        void deveRetornarModelAndViewParaMvc() {
            HttpServletRequest request = criarRequestMvc("/usuarios/editar/1");
            RecursoNaoEncontradoException ex = new RecursoNaoEncontradoException("Usuario", 1L);

            Object resultado = handler.handleRecursoNaoEncontrado(ex, request);

            assertThat(resultado).isInstanceOf(ModelAndView.class);
            ModelAndView mv = (ModelAndView) resultado;
            assertThat(mv.getViewName()).isEqualTo("error");
            assertThat(mv.getModel().get("status")).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("RegraDeNegocioException")
    class RegraDeNegocio {

        @Test
        @DisplayName("Deve retornar JSON 422 para requisicao API")
        @SuppressWarnings("unchecked")
        void deveRetornarJson422ParaApi() {
            HttpServletRequest request = criarRequestApi("/api/usuarios");
            RegraDeNegocioException ex = new RegraDeNegocioException("Email duplicado");

            Object resultado = handler.handleRegraDeNegocio(ex, request);

            assertThat(resultado).isInstanceOf(ResponseEntity.class);
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) resultado;
            assertThat(response.getStatusCode().value()).isEqualTo(422);
        }

        @Test
        @DisplayName("Deve retornar ModelAndView para requisicao MVC")
        void deveRetornarModelAndViewParaMvc() {
            HttpServletRequest request = criarRequestMvc("/usuarios/salvar");
            RegraDeNegocioException ex = new RegraDeNegocioException("Email duplicado");

            Object resultado = handler.handleRegraDeNegocio(ex, request);

            assertThat(resultado).isInstanceOf(ModelAndView.class);
            ModelAndView mv = (ModelAndView) resultado;
            assertThat(mv.getModel().get("status")).isEqualTo(422);
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException")
    class ArgumentoIlegal {

        @Test
        @DisplayName("Deve retornar JSON 400 para requisicao API")
        @SuppressWarnings("unchecked")
        void deveRetornarJson400ParaApi() {
            HttpServletRequest request = criarRequestApi("/api/usuarios/-1");
            IllegalArgumentException ex = new IllegalArgumentException("ID invalido");

            Object resultado = handler.handleIllegalArgument(ex, request);

            assertThat(resultado).isInstanceOf(ResponseEntity.class);
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) resultado;
            assertThat(response.getStatusCode().value()).isEqualTo(400);
        }

        @Test
        @DisplayName("Deve retornar ModelAndView para requisicao MVC")
        void deveRetornarModelAndViewParaMvc() {
            HttpServletRequest request = criarRequestMvc("/usuarios");
            IllegalArgumentException ex = new IllegalArgumentException("Parametro invalido");

            Object resultado = handler.handleIllegalArgument(ex, request);

            assertThat(resultado).isInstanceOf(ModelAndView.class);
            ModelAndView mv = (ModelAndView) resultado;
            assertThat(mv.getModel().get("status")).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Exception Generica")
    class ErroGenerico {

        @Test
        @DisplayName("Deve retornar JSON 500 com mensagem generica para API")
        @SuppressWarnings("unchecked")
        void deveRetornarJson500ComMensagemGenericaParaApi() {
            HttpServletRequest request = criarRequestApi("/api/qualquer");
            Exception ex = new RuntimeException("Erro interno detalhado que NAO deve ser exposto");

            Object resultado = handler.handleErroGenerico(ex, request);

            assertThat(resultado).isInstanceOf(ResponseEntity.class);
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) resultado;
            assertThat(response.getStatusCode().value()).isEqualTo(500);

            Map<String, Object> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.get("erro").toString()).doesNotContain("detalhado");
            assertThat(body.get("erro").toString()).contains("Tente novamente");
        }

        @Test
        @DisplayName("Deve retornar ModelAndView com mensagem generica para MVC")
        void deveRetornarModelAndViewComMensagemGenericaParaMvc() {
            HttpServletRequest request = criarRequestMvc("/usuarios");
            Exception ex = new RuntimeException("Detalhes internos secretos");

            Object resultado = handler.handleErroGenerico(ex, request);

            assertThat(resultado).isInstanceOf(ModelAndView.class);
            ModelAndView mv = (ModelAndView) resultado;
            assertThat(mv.getModel().get("status")).isEqualTo(500);
            assertThat(mv.getModel().get("mensagem").toString()).doesNotContain("secretos");
        }
    }

    /** Cria um mock de HttpServletRequest simulando requisicao API (JSON). */
    private HttpServletRequest criarRequestApi(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getHeader("Accept")).thenReturn("application/json");
        return request;
    }

    /** Cria um mock de HttpServletRequest simulando requisicao MVC (HTML). */
    private HttpServletRequest criarRequestMvc(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getHeader("Accept")).thenReturn("text/html");
        return request;
    }
}
