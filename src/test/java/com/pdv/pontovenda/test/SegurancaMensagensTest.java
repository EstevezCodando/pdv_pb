package com.pdv.pontovenda.test;

import com.pdv.pontovenda.repository.ProdutoRepository;
import com.pdv.pontovenda.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de seguranca de mensagens — garante que nenhuma resposta de erro
 * expoe informacoes internas do sistema (stack traces, nomes de classes,
 * detalhes de banco de dados, versoes de framework, etc.).
 *
 * Valida requisitos:
 *   - Mensagens de erro claras e seguras na interface web.
 *   - Evita exposicao de informacoes sensiveis.
 *   - Feedback apropriado em cenarios adversos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Seguranca de Mensagens — Nao Expor Informacoes Sensiveis")
class SegurancaMensagensTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    /** Termos que NUNCA devem aparecer em respostas de erro ao usuario. */
    private static final String[] TERMOS_PROIBIDOS = {
            "stackTrace", "java.lang", "org.springframework", "org.hibernate",
            "com.pdv.pontovenda", "NullPointerException", "SQLException",
            "DataIntegrityViolationException", "JdbcSQLIntegrityConstraintViolationException",
            "at com.", "at org.", "at java.", "Caused by:", ".java:",
            "password", "jdbc:", "h2:mem", "root@localhost"
    };

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("Respostas API JSON")
    class RespostasApi {

        @Test
        @DisplayName("Erro 404 na API nao deve expor informacoes internas")
        void erro404NaoDeveExporInformacoesInternas() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/usuarios/99999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn();

            verificarAusenciaDeTermosProibidos(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Erro de validacao na API nao deve expor informacoes internas")
        void erroValidacaoNaoDeveExporInformacoesInternas() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            verificarAusenciaDeTermosProibidos(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Erro de regra de negocio na API nao deve expor informacoes internas")
        void erroRegraDeNegocioNaoDeveExporInformacoesInternas() throws Exception {
            // Cria usuario
            mockMvc.perform(post("/api/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"nome":"Teste","email":"sec@test.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                            """));

            // Tenta duplicar email
            MvcResult result = mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nome":"Teste2","email":"sec@test.com","senha":"outra123","perfil":"ADMIN","ativo":true}
                                    """))
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn();

            verificarAusenciaDeTermosProibidos(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Erro com JSON malformado nao deve expor detalhes do parser")
        void erroJsonMalformadoNaoDeveExporDetalhesDoParser() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{json invalido sem aspas!!!}"))
                    .andExpect(status().is4xxClientError())
                    .andReturn();

            String body = result.getResponse().getContentAsString();
            assertThat(body).doesNotContain("JsonParseException");
            assertThat(body).doesNotContain("com.fasterxml.jackson");
        }
    }

    @Nested
    @DisplayName("Respostas MVC HTML")
    class RespostasMvc {

        @Test
        @DisplayName("Pagina de erro 404 nao deve conter Whitelabel ou stack trace")
        void paginaErro404NaoDeveConterWhitelabel() throws Exception {
            MvcResult result = mockMvc.perform(get("/pagina/totalmente/inexistente"))
                    .andExpect(status().isNotFound())
                    .andReturn();

            String html = result.getResponse().getContentAsString();
            assertThat(html).doesNotContain("Whitelabel Error Page");
            verificarAusenciaDeTermosProibidos(html);
        }

        @ParameterizedTest(name = "Rota MVC com payload #{index}: {0}")
        @DisplayName("Formularios web nao devem expor informacoes internas em cenarios de erro")
        @ValueSource(strings = {
                "<script>alert(1)</script>",
                "'; DROP TABLE usuario;--",
                "{{constructor.constructor('return this')()}}"
        })
        void formulariosNaoDevemExporInformacoesInternas(String payload) throws Exception {
            MvcResult result = mockMvc.perform(post("/usuarios/salvar")
                            .param("nome", payload)
                            .param("email", "test@safe.com")
                            .param("senha", "senha123")
                            .param("perfil", "ADMIN")
                            .param("ativo", "true"))
                    .andReturn();

            String html = result.getResponse().getContentAsString();
            verificarAusenciaDeTermosProibidos(html);
        }

        @Test
        @DisplayName("Editar usuario inexistente redireciona sem expor informacoes")
        void editarUsuarioInexistenteRedirecionaSemExporInformacoes() throws Exception {
            mockMvc.perform(get("/usuarios/editar/99999"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attribute("mensagemErro",
                            not(containsString("org.springframework"))))
                    .andExpect(flash().attribute("mensagemErro",
                            not(containsString("Exception"))));
        }

        @Test
        @DisplayName("Excluir usuario inexistente redireciona com mensagem segura")
        void excluirUsuarioInexistenteRedirecionaComMensagemSegura() throws Exception {
            mockMvc.perform(get("/usuarios/excluir/99999"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attribute("mensagemErro",
                            containsString("99999")))
                    .andExpect(flash().attribute("mensagemErro",
                            not(containsString("stackTrace"))));
        }
    }

    /** Verifica que nenhum dos termos proibidos aparece no conteudo da resposta. */
    private void verificarAusenciaDeTermosProibidos(String conteudo) {
        for (String termo : TERMOS_PROIBIDOS) {
            assertThat(conteudo)
                    .as("Resposta nao deve conter o termo proibido: '%s'", termo)
                    .doesNotContain(termo);
        }
    }
}
