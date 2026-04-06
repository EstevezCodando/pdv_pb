package com.pdv.pontovenda.controller;

import com.pdv.pontovenda.dto.ResumoIntegradoResponse;
import com.pdv.pontovenda.service.ResumoIntegradoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exibe informacoes integradas dos modulos da aplicacao em um endpoint unico de leitura.
 */
@RestController
@RequestMapping("/api/integracao")
public class IntegracaoApiController {

    private final ResumoIntegradoService resumoIntegradoService;

    public IntegracaoApiController(ResumoIntegradoService resumoIntegradoService) {
        this.resumoIntegradoService = resumoIntegradoService;
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoIntegradoResponse> resumo() {
        return ResponseEntity.ok(resumoIntegradoService.gerarResumo());
    }
}
