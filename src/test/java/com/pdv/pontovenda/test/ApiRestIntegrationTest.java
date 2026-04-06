package com.pdv.pontovenda.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.repository.ProdutoRepository;
import com.pdv.pontovenda.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integracao para os controllers REST (/api/usuarios e /api/produtos).
 * Cobre todas as ramificacoes: CRUD completo, validacao, erros e edge cases.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("API REST — Testes de Integracao Completos")
class ApiRestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("API de Usuarios")
    class ApiUsuarios {

        @Test
        @DisplayName("GET /api/usuarios - Deve retornar lista vazia")
        void deveRetornarListaVazia() throws Exception {
            mockMvc.perform(get("/api/usuarios")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/usuarios - Deve retornar lista com usuarios")
        void deveRetornarListaComUsuarios() throws Exception {
            usuarioRepository.save(new Usuario(null, "Api User", "api@test.com", "senha123", "ADMIN", true));

            mockMvc.perform(get("/api/usuarios")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nome").value("Api User"));
        }

        @Test
        @DisplayName("GET /api/usuarios/{id} - Deve retornar usuario por ID")
        void deveRetornarUsuarioPorId() throws Exception {
            Usuario salvo = usuarioRepository.save(
                    new Usuario(null, "Busca", "busca@test.com", "senha123", "OPERADOR", true));

            mockMvc.perform(get("/api/usuarios/" + salvo.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Busca"))
                    .andExpect(jsonPath("$.email").value("busca@test.com"));
        }

        @Test
        @DisplayName("POST /api/usuarios - Deve criar usuario com sucesso")
        void deveCriarUsuarioComSucesso() throws Exception {
            String json = """
                    {"nome":"Novo Api","email":"novo@api.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                    """;

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nome").value("Novo Api"));
        }

        @Test
        @DisplayName("PUT /api/usuarios/{id} - Deve atualizar usuario com sucesso")
        void deveAtualizarUsuarioComSucesso() throws Exception {
            Usuario salvo = usuarioRepository.save(
                    new Usuario(null, "Antes", "antes@api.com", "senha123", "ADMIN", true));

            String json = """
                    {"nome":"Depois","email":"depois@api.com","senha":"novasenha","perfil":"OPERADOR","ativo":false}
                    """;

            mockMvc.perform(put("/api/usuarios/" + salvo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Depois"))
                    .andExpect(jsonPath("$.email").value("depois@api.com"));
        }

        @Test
        @DisplayName("DELETE /api/usuarios/{id} - Deve excluir usuario com sucesso")
        void deveExcluirUsuarioComSucesso() throws Exception {
            Usuario salvo = usuarioRepository.save(
                    new Usuario(null, "Excluir", "exc@api.com", "senha123", "ADMIN", true));

            mockMvc.perform(delete("/api/usuarios/" + salvo.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/usuarios/" + salvo.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/usuarios/{id} com ID negativo deve retornar 400")
        void deveRetornar400ParaPutComIdNegativo() throws Exception {
            String json = """
                    {"nome":"Teste","email":"t@t.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                    """;
            mockMvc.perform(put("/api/usuarios/-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/usuarios/{id} com ID negativo deve retornar 400")
        void deveRetornar400ParaDeleteComIdNegativo() throws Exception {
            mockMvc.perform(delete("/api/usuarios/-1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("API de Produtos")
    class ApiProdutos {

        @Test
        @DisplayName("GET /api/produtos - Deve retornar lista vazia")
        void deveRetornarListaVazia() throws Exception {
            mockMvc.perform(get("/api/produtos")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/produtos - Deve retornar lista com produtos")
        void deveRetornarListaComProdutos() throws Exception {
            produtoRepository.save(new Produto(null, "Api Prod", "d", new BigDecimal("10"), 5, "API01", true));

            mockMvc.perform(get("/api/produtos")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("GET /api/produtos/{id} - Deve retornar produto por ID")
        void deveRetornarProdutoPorId() throws Exception {
            Produto salvo = produtoRepository.save(
                    new Produto(null, "Busca Prod", "d", new BigDecimal("5"), 10, "BUSC01", true));

            mockMvc.perform(get("/api/produtos/" + salvo.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Busca Prod"));
        }

        @Test
        @DisplayName("POST /api/produtos - Deve criar produto com sucesso")
        void deveCriarProdutoComSucesso() throws Exception {
            String json = """
                    {"nome":"Novo Prod","descricao":"desc","preco":15.50,"quantidadeEstoque":100,"codigoBarras":"NP01","ativo":true}
                    """;

            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @Test
        @DisplayName("PUT /api/produtos/{id} - Deve atualizar produto com sucesso")
        void deveAtualizarProdutoComSucesso() throws Exception {
            Produto salvo = produtoRepository.save(
                    new Produto(null, "Antes", "d", new BigDecimal("5"), 10, "UPD01", true));

            String json = """
                    {"nome":"Depois","descricao":"nova","preco":20.00,"quantidadeEstoque":50,"codigoBarras":"UPD01","ativo":true}
                    """;

            mockMvc.perform(put("/api/produtos/" + salvo.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Depois"));
        }

        @Test
        @DisplayName("DELETE /api/produtos/{id} - Deve excluir produto com sucesso")
        void deveExcluirProdutoComSucesso() throws Exception {
            Produto salvo = produtoRepository.save(
                    new Produto(null, "Excluir", "d", new BigDecimal("5"), 10, "DEL01", true));

            mockMvc.perform(delete("/api/produtos/" + salvo.getId())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("PUT /api/produtos/{id} com ID negativo deve retornar 400")
        void deveRetornar400ParaPutComIdNegativo() throws Exception {
            String json = """
                    {"nome":"Teste","preco":1.00,"quantidadeEstoque":1,"ativo":true}
                    """;
            mockMvc.perform(put("/api/produtos/-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /api/produtos/{id} com ID negativo deve retornar 400")
        void deveRetornar400ParaDeleteComIdNegativo() throws Exception {
            mockMvc.perform(delete("/api/produtos/-1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/produtos/{id} inexistente deve retornar 404")
        void deveRetornar404ParaProdutoInexistente() throws Exception {
            mockMvc.perform(get("/api/produtos/99999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/produtos/{id} com codigo de barras duplicado deve retornar 422")
        void deveRetornar422ComCodigoBarrasDuplicado() throws Exception {
            produtoRepository.save(new Produto(null, "Existente", "d", new BigDecimal("5"), 10, "DUP01", true));
            Produto outro = produtoRepository.save(
                    new Produto(null, "Outro", "d", new BigDecimal("5"), 10, "ORIG01", true));

            String json = """
                    {"nome":"Outro Att","descricao":"d","preco":5.00,"quantidadeEstoque":10,"codigoBarras":"DUP01","ativo":true}
                    """;

            mockMvc.perform(put("/api/produtos/" + outro.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnprocessableEntity());
        }
    }
}
