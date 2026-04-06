package com.pdv.pontovenda.controller;

import com.pdv.pontovenda.dto.CriarVendaRequest;
import com.pdv.pontovenda.dto.ResumoVendaResponse;
import com.pdv.pontovenda.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST para o fluxo principal do PDV: fechamento de venda.
 */
@RestController
@RequestMapping("/api/vendas")
public class VendaApiController {

    private final VendaService vendaService;

    public VendaApiController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @GetMapping
    public ResponseEntity<List<ResumoVendaResponse>> listarTodas() {
        return ResponseEntity.ok(vendaService.listarTodas());
    }

    @PostMapping
    public ResponseEntity<ResumoVendaResponse> registrar(@Valid @RequestBody CriarVendaRequest requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendaService.registrarVenda(requisicao));
    }
}
