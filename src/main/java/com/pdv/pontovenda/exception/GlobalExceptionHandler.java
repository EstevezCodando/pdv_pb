package com.pdv.pontovenda.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handler global de exceções — implementa o padrão fail gracefully.
 * Intercepta exceções não tratadas pelos controllers e retorna:
 *   - Para requisições web (MVC): pagina de erro segura, sem stack trace.
 *   - Para requisições API (REST/JSON): JSON com mensagem segura e codigo HTTP.
 * Nenhuma informação técnica é exposta ao usuário final (segurança).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MENSAGEM_ERRO_GENERICO = "Ocorreu um erro interno. Tente novamente mais tarde.";
    private static final String MENSAGEM_CORPO_INVALIDO = "Corpo da requisicao invalido.";
    private static final String MENSAGEM_CONTEUDO_NAO_SUPORTADO = "Content-Type nao suportado para esta operacao.";
    private static final String MENSAGEM_METODO_NAO_SUPORTADO = "Metodo HTTP nao suportado para este endpoint.";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public Object handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        logger.warn("Recurso nao encontrado: {}", ex.getMessage());
        return responder(request, HttpStatus.NOT_FOUND, ex.getMessage(), "Pagina nao encontrada");
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public Object handleRegraDeNegocio(RegraDeNegocioException ex, HttpServletRequest request) {
        logger.warn("Regra de negocio violada: {}", ex.getMessage());
        return responder(request, HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        logger.warn("Erro de validacao: {}", ex.getMessage());

        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));

        return ResponseEntity.badRequest().body(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "erro", "Dados invalidos",
                "campos", campos
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        logger.warn("Metodo nao suportado em {}: {}", request.getRequestURI(), ex.getMethod());
        return responder(request, HttpStatus.METHOD_NOT_ALLOWED, MENSAGEM_METODO_NAO_SUPORTADO,
                "Operacao nao suportada");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Object handleMediaTypeNaoSuportado(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        logger.warn("Content-Type nao suportado em {}", request.getRequestURI());
        return responder(request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, MENSAGEM_CONTEUDO_NAO_SUPORTADO,
                "Conteudo da requisicao nao suportado");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleMensagemNaoLegivel(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("Corpo de requisicao invalido em {}", request.getRequestURI());
        return responder(request, HttpStatus.BAD_REQUEST, MENSAGEM_CORPO_INVALIDO,
                "Requisicao invalida");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object handleTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.warn("Tipo de argumento invalido em {}: {}", request.getRequestURI(), ex.getName());
        return responder(request, HttpStatus.BAD_REQUEST, "Parametro invalido.", "Parametro invalido");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        logger.warn("Rota nao encontrada: {}", request.getRequestURI());
        return responder(request, HttpStatus.NOT_FOUND, "Recurso nao encontrado", "Pagina nao encontrada");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Argumento invalido: {}", ex.getMessage());
        return responder(request, HttpStatus.BAD_REQUEST, ex.getMessage(), "Requisicao invalida: " + ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.warn("Violacao de integridade em {}", request.getRequestURI());
        return responder(request, HttpStatus.UNPROCESSABLE_ENTITY,
                "Nao foi possivel concluir a operacao com os dados informados.",
                "Nao foi possivel concluir a operacao com os dados informados.");
    }

    @ExceptionHandler(Exception.class)
    public Object handleErroGenerico(Exception ex, HttpServletRequest request) {
        logger.error("Erro inesperado em {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return responder(request, HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_ERRO_GENERICO, MENSAGEM_ERRO_GENERICO);
    }

    private Object responder(HttpServletRequest request, HttpStatus status, String mensagemApi, String mensagemMvc) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(status).body(criarErroApi(status.value(), mensagemApi));
        }

        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", status.value());
        mv.addObject("mensagem", mensagemMvc);
        mv.setStatus(status);
        return mv;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        return uri.startsWith("/api/")
                || (accept != null && accept.contains("application/json"));
    }

    private Map<String, Object> criarErroApi(int status, String mensagem) {
        return Map.of("status", status, "erro", mensagem);
    }
}
