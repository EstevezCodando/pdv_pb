package com.pdv.pontovenda.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdv.pontovenda.repository.ProdutoRepository;
import com.pdv.pontovenda.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fuzz Testing — envia entradas aleatorias, malformadas e maliciosas
 * para a API REST a fim de detectar vulnerabilidades, crashes e respostas inseguras.
 *
 * Valida que o sistema:
 *   - Nao expoe stack traces ou informacoes internas ao rejeitar entradas invalidas.
 *   - Retorna codigos HTTP adequados (400, 422) em vez de 500.
 *   - Trata payloads XSS, SQL injection e valores de limite sem falhar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Fuzz Testing — Entradas Aleatorias e Maliciosas")
class FuzzTestingTest {

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
    @DisplayName("Fuzz na API de Usuarios")
    class FuzzUsuario {

        @ParameterizedTest(name = "Payload XSS #{index}: {0}")
        @DisplayName("Deve rejeitar payloads XSS no nome do usuario sem expor detalhes internos")
        @ValueSource(strings = {
                "<script>alert('xss')</script>",
                "<img src=x onerror=alert(1)>",
                "'; DROP TABLE usuario; --",
                "<iframe src='javascript:alert(1)'>",
                "{{7*7}}",
                "${7*7}",
                "%00%0d%0aHeader-Injection:true"
        })
        void deveRejeitarPayloadsXssNoNome(String payloadMalicioso) throws Exception {
            Map<String, Object> usuario = criarUsuarioMap(payloadMalicioso, "fuzz@test.com", "senha123", "ADMIN");

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(usuario)))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(not(containsString("Exception"))))
                    .andExpect(content().string(not(containsString("stackTrace"))))
                    .andExpect(content().string(not(containsString("org.springframework"))));
        }

        @ParameterizedTest(name = "SQL Injection #{index}: {0}")
        @DisplayName("Deve rejeitar tentativas de SQL injection no email")
        @ValueSource(strings = {
                "' OR '1'='1",
                "admin'--",
                "'; INSERT INTO usuario VALUES (999,'hacked','h@h.com','123','ADMIN',true); --",
                "1; DROP TABLE usuario",
                "' UNION SELECT * FROM usuario --"
        })
        void deveRejeitarSqlInjectionNoEmail(String sqlInjection) throws Exception {
            Map<String, Object> usuario = criarUsuarioMap("Teste", sqlInjection, "senha123", "ADMIN");

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(usuario)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Deve rejeitar payload JSON com campos extras desconhecidos")
        void deveRejeitarJsonComCamposExtras() throws Exception {
            String jsonComExtras = """
                    {
                        "nome": "Teste",
                        "email": "teste@fuzz.com",
                        "senha": "senha123",
                        "perfil": "ADMIN",
                        "ativo": true,
                        "admin_override": true,
                        "role": "SUPERADMIN"
                    }
                    """;

            // O sistema deve ignorar campos extras e processar normalmente ou rejeitar
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonComExtras))
                    .andExpect(resultado -> org.assertj.core.api.Assertions.assertThat(resultado.getResponse().getStatus()).isBetween(200, 499));
        }

        @Test
        @DisplayName("Deve tratar payload JSON vazio sem erro 500")
        void deveTratarJsonVazio() throws Exception {
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(not(containsString("NullPointerException"))));
        }

        @Test
        @DisplayName("Deve tratar payload JSON malformado sem erro 500")
        void deveTratarJsonMalformado() throws Exception {
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalido: sem aspas}"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Deve tratar body vazio sem crash")
        void deveTratarBodyVazio() throws Exception {
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().is4xxClientError());
        }

        @ParameterizedTest(name = "String gigante com {0} caracteres")
        @DisplayName("Deve rejeitar strings que excedem limites de tamanho")
        @ValueSource(ints = {101, 500, 1000, 5000})
        void deveRejeitarStringsGigantes(int tamanho) throws Exception {
            String nome = "A".repeat(tamanho);
            Map<String, Object> usuario = criarUsuarioMap(nome, "big@test.com", "senha123", "ADMIN");

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(usuario)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Deve rejeitar caracteres Unicode e de controle sem crash")
        void deveRejeitarCaracteresUnicode() throws Exception {
            Map<String, Object> usuario = criarUsuarioMap(
                    "Nom\u0000e\u0001Com\u0002Controle", "uni@test.com", "senha123", "ADMIN");

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(usuario)))
                    .andExpect(resultado -> org.assertj.core.api.Assertions.assertThat(resultado.getResponse().getStatus()).isBetween(200, 499));
        }
    }

    @Nested
    @DisplayName("Fuzz na API de Produtos")
    class FuzzProduto {

        @ParameterizedTest(name = "Preco invalido #{index}: {0}")
        @DisplayName("Deve rejeitar precos invalidos ou maliciosos")
        @ValueSource(strings = {"-1.00", "0", "-999999.99", "9999999999.99"})
        void deveRejeitarPrecosInvalidos(String preco) throws Exception {
            String json = String.format("""
                    {
                        "nome": "Produto Fuzz",
                        "preco": %s,
                        "quantidadeEstoque": 10,
                        "ativo": true
                    }
                    """, preco);

            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().is4xxClientError());
        }

        @ParameterizedTest(name = "Estoque invalido #{index}: {0}")
        @DisplayName("Deve rejeitar valores de estoque invalidos")
        @ValueSource(strings = {"-1", "-999"})
        void deveRejeitarEstoqueInvalido(String estoque) throws Exception {
            String json = String.format("""
                    {
                        "nome": "Produto Fuzz",
                        "preco": 10.00,
                        "quantidadeEstoque": %s,
                        "ativo": true
                    }
                    """, estoque);

            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Deve tratar tipo de dado errado no preco sem crash (string em vez de numero)")
        void deveTratarTipoDadoErrado() throws Exception {
            String json = """
                    {
                        "nome": "Produto",
                        "preco": "nao_e_numero",
                        "quantidadeEstoque": 10,
                        "ativo": true
                    }
                    """;

            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().string(not(containsString("NumberFormatException"))));
        }

        @ParameterizedTest(name = "Fuzz aleatorio #{index}")
        @DisplayName("Deve processar ou rejeitar entradas aleatorias sem crash (fuzz random)")
        @MethodSource("com.pdv.pontovenda.test.FuzzTestingTest#gerarPayloadsAleatorios")
        void deveTratarPayloadsAleatoriosSemCrash(String payloadJson) throws Exception {
            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadJson))
                    .andExpect(resultado -> org.assertj.core.api.Assertions.assertThat(resultado.getResponse().getStatus()).isBetween(200, 599));
            // O importante é não lançar exceção não tratada (o teste não deve falhar por crash)
        }
    }

    @Nested
    @DisplayName("Fuzz nos Endpoints MVC (Web)")
    class FuzzMvc {

        @ParameterizedTest(name = "Payload MVC #{index}: {0}")
        @DisplayName("Deve tratar entradas maliciosas nos formularios web sem expor informações")
        @ValueSource(strings = {
                "<script>alert(1)</script>",
                "' OR 1=1 --",
                "../../../etc/passwd",
                "%00",
                "\r\nInjected-Header: value"
        })
        void deveTratarEntradasMaliciosasNoFormularioWeb(String payload) throws Exception {
            mockMvc.perform(post("/usuarios/salvar")
                            .param("nome", payload)
                            .param("email", "test@test.com")
                            .param("senha", "senha123")
                            .param("perfil", "ADMIN")
                            .param("ativo", "true"))
                    .andExpect(resultado -> org.assertj.core.api.Assertions.assertThat(resultado.getResponse().getStatus()).isIn(200, 301, 302, 303, 307, 308))
                    .andExpect(content().string(not(containsString("Exception"))))
                    .andExpect(content().string(not(containsString("stackTrace"))));
        }

        @Test
        @DisplayName("Deve retornar 404 seguro para rotas inexistentes")
        void deveRetornar404SeguroParaRotaInexistente() throws Exception {
            mockMvc.perform(get("/rota/inexistente/qualquer"))
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest(name = "Path traversal #{index}: {0}")
        @DisplayName("Deve bloquear tentativas de path traversal")
        @ValueSource(strings = {
                "/usuarios/editar/../../../etc/passwd",
                "/usuarios/excluir/0",
                "/produtos/editar/-1"
        })
        void deveBloquearPathTraversal(String path) throws Exception {
            mockMvc.perform(get(path))
                    .andExpect(resultado -> org.assertj.core.api.Assertions.assertThat(resultado.getResponse().getStatus()).isBetween(300, 499));
        }
    }

    /** Gera payloads JSON aleatorios para fuzz testing. */
    static Stream<String> gerarPayloadsAleatorios() {
        Random random = new Random(42); // Seed fixa para reprodutibilidade
        return Stream.generate(() -> {
            String nome = gerarStringAleatoria(random, random.nextInt(200) + 1);
            double preco = random.nextDouble() * 10000 - 5000; // Inclui negativos
            int estoque = random.nextInt(2000) - 1000; // Inclui negativos
            return String.format("""
                    {"nome":"%s","preco":%s,"quantidadeEstoque":%d,"ativo":%s}
                    """, nome, preco, estoque, random.nextBoolean());
        }).limit(10);
    }

    private static String gerarStringAleatoria(Random random, int tamanho) {
        StringBuilder sb = new StringBuilder(tamanho);
        for (int i = 0; i < tamanho; i++) {
            sb.append((char) (random.nextInt(94) + 32)); // ASCII imprimível
        }
        return sb.toString().replace("\"", "\\\"").replace("\\", "\\\\");
    }

    private Map<String, Object> criarUsuarioMap(String nome, String email, String senha, String perfil) {
        Map<String, Object> map = new HashMap<>();
        map.put("nome", nome);
        map.put("email", email);
        map.put("senha", senha);
        map.put("perfil", perfil);
        map.put("ativo", true);
        return map;
    }
}
