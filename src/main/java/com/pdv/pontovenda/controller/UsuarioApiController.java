package com.pdv.pontovenda.controller;

import com.pdv.pontovenda.config.ValidadorDeIdentificador;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.service.UsuarioService;
import com.pdv.pontovenda.validation.ValidadorEntradaSegura;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API para operacoes CRUD de Usuario.
 * Endpoints expostos sob /api/usuarios para integracao e testes automatizados.
 * Aplica fail early: valida entradas antes de processar.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioApiController {

    private final UsuarioService usuarioService;
    private final ValidadorDeIdentificador validadorDeIdentificador;
    private final ValidadorEntradaSegura validadorEntradaSegura;

    public UsuarioApiController(UsuarioService usuarioService,
                                ValidadorDeIdentificador validadorDeIdentificador,
                                ValidadorEntradaSegura validadorEntradaSegura) {
        this.usuarioService = usuarioService;
        this.validadorDeIdentificador = validadorDeIdentificador;
        this.validadorEntradaSegura = validadorEntradaSegura;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        validadorDeIdentificador.validarPositivo(id, "ID");
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody Usuario usuario) {
        validadorEntradaSegura.validarNomeUsuario(usuario.getNome());
        Usuario salvo = usuarioService.salvar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        validadorDeIdentificador.validarPositivo(id, "ID");
        validadorEntradaSegura.validarNomeUsuario(usuario.getNome());
        Usuario atualizado = usuarioService.atualizar(id, usuario);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        validadorDeIdentificador.validarPositivo(id, "ID");
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
