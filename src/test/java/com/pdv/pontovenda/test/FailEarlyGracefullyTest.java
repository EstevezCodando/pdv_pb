package com.pdv.pontovenda.test;

import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import com.pdv.pontovenda.repository.ProdutoRepository;
import com.pdv.pontovenda.repository.UsuarioRepository;
import com.pdv.pontovenda.service.ProdutoService;
import com.pdv.pontovenda.service.UsuarioService;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes que validam os padroes fail early e fail gracefully:
 *
 * Fail Early: o sistema valida entradas o mais cedo possivel e rejeita
 *   dados invalidos ANTES de atingir camadas mais profundas (banco, processamento).
 *
 * Fail Gracefully: quando ocorre um erro inesperado ou esperado, o sistema
 *   retorna uma resposta controlada sem expor detalhes internos, stack traces
 *   ou informacoes sensiveis.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Fail Early e Fail Gracefully — Robustez do Sistema")
class FailEarlyGracefullyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProdutoService produtoService;

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
    @DisplayName("Fail Early — Validacao Antecipada de Entradas")
    class FailEarly {

        @Test
        @DisplayName("Deve rejeitar ID negativo na API antes de consultar o banco")
        void deveRejeitarIdNegativoNaApi() throws Exception {
            mockMvc.perform(get("/api/usuarios/-1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro").value("ID deve ser um numero positivo"));
        }

        @Test
        @DisplayName("Deve rejeitar ID zero na API de produtos antes de consultar o banco")
        void deveRejeitarIdZeroNaApiProdutos() throws Exception {
            mockMvc.perform(get("/api/produtos/0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro").value("ID deve ser um numero positivo"));
        }

        @Test
        @DisplayName("Deve rejeitar dados incompletos via Bean Validation antes de atingir o service")
        void deveRejeitarDadosIncompletosAntesDoChegarNoService() throws Exception {
            String jsonIncompleto = """
                    {
                        "nome": "",
                        "email": "",
                        "senha": "",
                        "perfil": ""
                    }
                    """;

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonIncompleto))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos").exists())
                    .andExpect(jsonPath("$.campos.nome").exists())
                    .andExpect(jsonPath("$.campos.email").exists());
        }

        @Test
        @DisplayName("Deve rejeitar preco zero no produto via Bean Validation")
        void deveRejeitarPrecoZeroNoProduto() throws Exception {
            String json = """
                    {
                        "nome": "Produto Invalido",
                        "preco": 0.00,
                        "quantidadeEstoque": 10,
                        "ativo": true
                    }
                    """;

            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.preco").exists());
        }

        @Test
        @DisplayName("Deve validar email duplicado no service (fail early na regra de negocio)")
        void deveValidarEmailDuplicadoNoService() {
            Usuario u1 = new Usuario(null, "User A", "dup@test.com", "senha123", "ADMIN", true);
            usuarioService.salvar(u1);

            Usuario u2 = new Usuario(null, "User B", "dup@test.com", "outra123", "OPERADOR", true);

            assertThatThrownBy(() -> usuarioService.salvar(u2))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("dup@test.com");
        }

        @Test
        @DisplayName("Deve validar codigo de barras duplicado no service (fail early)")
        void deveValidarCodigoBarrasDuplicadoNoService() {
            Produto p1 = new Produto(null, "Prod A", "d", new BigDecimal("5"), 10, "DUPCOD", true);
            produtoService.salvar(p1);

            Produto p2 = new Produto(null, "Prod B", "d", new BigDecimal("10"), 20, "DUPCOD", true);

            assertThatThrownBy(() -> produtoService.salvar(p2))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("DUPCOD");
        }
    }

    @Nested
    @DisplayName("Fail Gracefully — Resposta Controlada a Erros")
    class FailGracefully {

        @Test
        @DisplayName("Deve retornar 404 JSON controlado quando recurso nao existe na API")
        void deveRetornar404JsonControladoQuandoRecursoNaoExiste() throws Exception {
            mockMvc.perform(get("/api/usuarios/99999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.erro").value(containsString("99999")))
                    .andExpect(content().string(not(containsString("stackTrace"))))
                    .andExpect(content().string(not(containsString("org.springframework"))));
        }

        @Test
        @DisplayName("Deve retornar pagina de erro HTML segura para rota MVC inexistente")
        void deveRetornarPaginaErroHtmlSegura() throws Exception {
            mockMvc.perform(get("/pagina/que/nao/existe"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(not(containsString("Whitelabel"))))
                    .andExpect(content().string(not(containsString("stackTrace"))));
        }

        @Test
        @DisplayName("Deve redirecionar com mensagem de erro ao editar usuario inexistente (MVC)")
        void deveRedirecionarComMensagemErroAoEditarUsuarioInexistente() throws Exception {
            mockMvc.perform(get("/usuarios/editar/99999"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("mensagemErro"));
        }

        @Test
        @DisplayName("Deve retornar 404 JSON para produto inexistente na API")
        void deveRetornar404ParaProdutoInexistenteNaApi() throws Exception {
            mockMvc.perform(get("/api/produtos/99999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.erro").exists())
                    .andExpect(content().string(not(containsString("Exception"))));
        }

        @Test
        @DisplayName("Deve retornar 422 JSON ao violar regra de negocio na API")
        void deveRetornar422AoViolarRegraDeNegocioNaApi() throws Exception {
            // Cria primeiro usuario
            String json = """
                    {"nome":"Primeiro","email":"unico@test.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                    """;
            mockMvc.perform(post("/api/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));

            // Tenta criar segundo com mesmo email
            String json2 = """
                    {"nome":"Segundo","email":"unico@test.com","senha":"outra123","perfil":"OPERADOR","ativo":true}
                    """;
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json2))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.erro").value(containsString("unico@test.com")));
        }

        @Test
        @DisplayName("Service deve lancar excecao tipada ao buscar recurso inexistente")
        void serviceLancaExcecaoTipadaAoBuscarRecursoInexistente() {
            assertThatThrownBy(() -> usuarioService.buscarPorId(99999L))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("99999");

            assertThatThrownBy(() -> produtoService.buscarPorId(99999L))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("99999");
        }

        @Test
        @DisplayName("Deve retornar erro seguro ao enviar content-type errado")
        void deveRetornarErroSeguroComContentTypeErrado() throws Exception {
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("isso nao e json"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Deve tratar DELETE de recurso inexistente com erro controlado")
        void deveTratarDeleteDeRecursoInexistente() throws Exception {
            mockMvc.perform(delete("/api/usuarios/99999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.erro").exists());
        }

        @Test
        @DisplayName("Deve tratar PUT em recurso inexistente com erro controlado")
        void deveTratarPutEmRecursoInexistente() throws Exception {
            String json = """
                    {"nome":"Atualizar","email":"att@test.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                    """;
            mockMvc.perform(put("/api/usuarios/99999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.erro").exists());
        }
    }
}
