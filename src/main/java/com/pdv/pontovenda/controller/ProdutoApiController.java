package com.pdv.pontovenda.controller;

import com.pdv.pontovenda.config.ValidadorDeIdentificador;
import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API para operacoes CRUD de Produto.
 * Endpoints expostos sob /api/produtos para integracao e testes automatizados.
 * Aplica fail early: valida entradas antes de processar.
 */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoApiController {

    private final ProdutoService produtoService;
    private final ValidadorDeIdentificador validadorDeIdentificador;

    public ProdutoApiController(ProdutoService produtoService,
                                ValidadorDeIdentificador validadorDeIdentificador) {
        this.produtoService = produtoService;
        this.validadorDeIdentificador = validadorDeIdentificador;
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        List<Produto> produtos = produtoService.listarTodos();
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        validadorDeIdentificador.validarPositivo(id, "ID");
        Produto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(produto);
    }

    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody Produto produto) {
        Produto salvo = produtoService.salvar(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @Valid @RequestBody Produto produto) {
        validadorDeIdentificador.validarPositivo(id, "ID");
        Produto atualizado = produtoService.atualizar(id, produto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        validadorDeIdentificador.validarPositivo(id, "ID");
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
