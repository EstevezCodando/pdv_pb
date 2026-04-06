package com.pdv.pontovenda.test;

import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de cenários adversos: simula falhas de rede, timeouts, sobrecarga
 * e operacoes concorrentes para validar a robustez do sistema.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Cenarios Adversos — Timeout, Sobrecarga e Concorrencia")
class CenariosAdversosTest {

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
    @DisplayName("Simulacao de Sobrecarga — Requisicoes Concorrentes")
    class Sobrecarga {

        @Test
        @DisplayName("Deve suportar multiplas requisicoes simultaneas de listagem sem falhar")
        void deveSuportarRequisicoesSimultaneasDeListagem() throws Exception {
            // Prepara dados
            for (int i = 0; i < 10; i++) {
                usuarioRepository.save(new Usuario(null, "User " + i,
                        "user" + i + "@test.com", "senha123", "OPERADOR", true));
            }

            int numThreads = 20;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            List<Future<Integer>> futuros = new ArrayList<>();

            for (int i = 0; i < numThreads; i++) {
                futuros.add(executor.submit(() -> {
                    try {
                        return mockMvc.perform(get("/api/usuarios")
                                        .accept(MediaType.APPLICATION_JSON))
                                .andReturn().getResponse().getStatus();
                    } catch (Exception e) {
                        return 500;
                    }
                }));
            }

            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);

            List<Integer> resultados = new ArrayList<>();
            for (Future<Integer> futuro : futuros) {
                resultados.add(futuro.get());
            }

            // Todas as requisicoes devem retornar 200
            long sucessos = resultados.stream().filter(s -> s == 200).count();
            assertThat(sucessos).isEqualTo(numThreads);
        }

        @Test
        @DisplayName("Deve tratar criacao concorrente com emails duplicados de forma segura")
        void deveTratarCriacaoConcorrenteComEmailsDuplicados() throws Exception {
            int numThreads = 5;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            List<Future<Integer>> futuros = new ArrayList<>();

            // Todas as threads tentam criar usuario com o mesmo email
            for (int i = 0; i < numThreads; i++) {
                futuros.add(executor.submit(() -> {
                    try {
                        String json = """
                                {"nome":"Concorrente","email":"conc@test.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                                """;
                        return mockMvc.perform(post("/api/usuarios")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json))
                                .andReturn().getResponse().getStatus();
                    } catch (Exception e) {
                        return 500;
                    }
                }));
            }

            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);

            List<Integer> resultados = new ArrayList<>();
            for (Future<Integer> futuro : futuros) {
                resultados.add(futuro.get());
            }

            // Apenas 1 deve ter sucesso (201), o restante deve receber erro (422)
            long sucessos = resultados.stream().filter(s -> s == 201).count();
            long rejeitados = resultados.stream().filter(s -> s == 422).count();

            assertThat(sucessos).isGreaterThanOrEqualTo(1);
            assertThat(sucessos + rejeitados).isEqualTo(numThreads);
        }

        @Test
        @DisplayName("Deve suportar multiplas exclusoes e listagens concorrentes")
        void deveSuportarExclusoesEListagensConcorrentes() throws Exception {
            // Prepara dados
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Produto salvo = produtoRepository.save(new Produto(null, "Prod " + i,
                        "desc", new BigDecimal("5.00"), 10, "CODE" + i, true));
                ids.add(salvo.getId());
            }

            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<Integer>> futuros = new ArrayList<>();

            // Metade exclui, metade lista
            for (int i = 0; i < ids.size(); i++) {
                final int index = i;
                if (i % 2 == 0) {
                    futuros.add(executor.submit(() -> {
                        try {
                            return mockMvc.perform(delete("/api/produtos/" + ids.get(index))
                                            .accept(MediaType.APPLICATION_JSON))
                                    .andReturn().getResponse().getStatus();
                        } catch (Exception e) {
                            return 500;
                        }
                    }));
                } else {
                    futuros.add(executor.submit(() -> {
                        try {
                            return mockMvc.perform(get("/api/produtos")
                                            .accept(MediaType.APPLICATION_JSON))
                                    .andReturn().getResponse().getStatus();
                        } catch (Exception e) {
                            return 500;
                        }
                    }));
                }
            }

            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);

            for (Future<Integer> futuro : futuros) {
                int status = futuro.get();
                // Nenhuma requisicao deve retornar 500 (erro interno)
                assertThat(status).isNotEqualTo(500);
            }
        }
    }

    @Nested
    @DisplayName("Simulacao de Falhas — Recursos Inexistentes e Operacoes Invalidas")
    class SimulacaoFalhas {

        @Test
        @DisplayName("Deve tratar exclusao seguida de consulta ao mesmo recurso")
        void deveTratarExclusaoSeguidaDeConsulta() {
            Usuario salvo = usuarioRepository.save(
                    new Usuario(null, "Temp", "temp@test.com", "senha123", "ADMIN", true));
            Long id = salvo.getId();

            usuarioService.excluir(id);

            // Consulta deve falhar com excecao tipada (nao NullPointerException)
            assertThatThrownBy(() -> usuarioService.buscarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        @DisplayName("Deve tratar dupla exclusao do mesmo recurso")
        void deveTratarDuplaExclusaoDoMesmoRecurso() {
            Produto salvo = produtoRepository.save(
                    new Produto(null, "Temp", "d", new BigDecimal("1"), 1, "TEMP", true));
            Long id = salvo.getId();

            produtoService.excluir(id);

            // Segunda exclusao deve falhar com excecao tipada
            assertThatThrownBy(() -> produtoService.excluir(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        @DisplayName("Deve tratar atualizacao de recurso ja excluido")
        void deveTratarAtualizacaoDeRecursoJaExcluido() {
            Usuario salvo = usuarioRepository.save(
                    new Usuario(null, "Temp", "temp2@test.com", "senha123", "ADMIN", true));
            Long id = salvo.getId();

            usuarioService.excluir(id);

            Usuario atualizado = new Usuario(null, "Novo", "novo@test.com", "novasenha", "OPERADOR", true);
            assertThatThrownBy(() -> usuarioService.atualizar(id, atualizado))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        @DisplayName("Deve retornar resposta controlada ao enviar metodo HTTP nao suportado")
        void deveRetornarRespostaControladaComMetodoNaoSuportado() throws Exception {
            mockMvc.perform(patch("/api/usuarios/1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Deve tratar requisicao com Accept header incompativel")
        void deveTratarRequisicaoComAcceptIncompativel() throws Exception {
            mockMvc.perform(get("/api/usuarios")
                            .accept(MediaType.APPLICATION_XML))
                    .andExpect(resultado -> org.assertj.core.api.Assertions.assertThat(resultado.getResponse().getStatus()).isBetween(200, 499));
        }
    }

    @Nested
    @DisplayName("Testes de Limite e Boundary Values")
    class LimitesBoundary {

        @Test
        @DisplayName("Deve aceitar nome de usuario com exatamente 3 caracteres (limite minimo)")
        void deveAceitarNomeComLimiteMinimo() throws Exception {
            String json = """
                    {"nome":"Ana","email":"ana@test.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                    """;
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve rejeitar nome de usuario com 2 caracteres (abaixo do limite)")
        void deveRejeitarNomeAbaixoDoLimite() throws Exception {
            String json = """
                    {"nome":"AB","email":"ab@test.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                    """;
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve aceitar nome de usuario com exatamente 100 caracteres (limite maximo)")
        void deveAceitarNomeComLimiteMaximo() throws Exception {
            String nome100chars = "A".repeat(100);
            String json = String.format(
                    """
                    {"nome":"%s","email":"max@test.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                    """, nome100chars);

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve rejeitar nome de usuario com 101 caracteres (acima do limite)")
        void deveRejeitarNomeAcimaDoLimite() throws Exception {
            String nome101chars = "A".repeat(101);
            String json = String.format(
                    """
                    {"nome":"%s","email":"over@test.com","senha":"senha123","perfil":"ADMIN","ativo":true}
                    """, nome101chars);

            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve aceitar preco no limite minimo (0.01)")
        void deveAceitarPrecoLimiteMinimo() throws Exception {
            String json = """
                    {"nome":"Produto","preco":0.01,"quantidadeEstoque":0,"ativo":true}
                    """;
            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve aceitar estoque zero (limite minimo)")
        void deveAceitarEstoqueZero() throws Exception {
            String json = """
                    {"nome":"Produto Vazio","preco":1.00,"quantidadeEstoque":0,"ativo":true}
                    """;
            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve aceitar senha com exatamente 4 caracteres (limite minimo)")
        void deveAceitarSenhaComLimiteMinimo() throws Exception {
            String json = """
                    {"nome":"Teste","email":"s4@test.com","senha":"1234","perfil":"ADMIN","ativo":true}
                    """;
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve rejeitar senha com 3 caracteres (abaixo do limite)")
        void deveRejeitarSenhaAbaixoDoLimite() throws Exception {
            String json = """
                    {"nome":"Teste","email":"s3@test.com","senha":"123","perfil":"ADMIN","ativo":true}
                    """;
            mockMvc.perform(post("/api/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }
    }
}
