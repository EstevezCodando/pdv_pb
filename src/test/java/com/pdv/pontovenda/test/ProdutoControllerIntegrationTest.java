package com.pdv.pontovenda.test;

import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.repository.ProdutoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integracao para ProdutoController.
 * Validam o fluxo completo: controller -> service -> repository.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProdutoController - Testes de Integracao")
class ProdutoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /produtos - Deve exibir pagina de listagem")
    void deveExibirPaginaDeListagem() throws Exception {
        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(view().name("produto/listagem"))
                .andExpect(model().attributeExists("produtos"));
    }

    @Test
    @DisplayName("GET /produtos - Deve listar produtos cadastrados")
    void deveListarProdutosCadastrados() throws Exception {
        produtoRepository.save(new Produto(null, "Arroz", "Arroz tipo 1", new BigDecimal("8.90"), 100, "1234567", true));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("produtos", hasSize(1)));
    }

    @Test
    @DisplayName("GET /produtos/novo - Deve exibir formulario de cadastro")
    void deveExibirFormularioDeCadastro() throws Exception {
        mockMvc.perform(get("/produtos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("produto/formulario"))
                .andExpect(model().attributeExists("produto", "acao"));
    }

    @Test
    @DisplayName("POST /produtos/salvar - Deve cadastrar produto valido e redirecionar")
    void deveCadastrarProdutoValidoERedirecionar() throws Exception {
        mockMvc.perform(post("/produtos/salvar")
                        .param("nome", "Feijao Preto")
                        .param("descricao", "Feijao preto tipo 1")
                        .param("preco", "7.50")
                        .param("quantidadeEstoque", "200")
                        .param("codigoBarras", "9999999")
                        .param("ativo", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));

        Assertions.assertEquals(1, produtoRepository.count());
    }

    @ParameterizedTest(name = "Teste {index}: nome={0}, preco={1}, estoque={2}")
    @DisplayName("POST /produtos/salvar - Deve rejeitar dados invalidos")
    @CsvSource({
            "'', 8.90, 100",        // nome vazio
            "A, 8.90, 100",         // nome muito curto
            "Arroz, 0.00, 100",     // preco zero
            "Arroz, -1.00, 100",    // preco negativo
            "Arroz, 8.90, -5"       // estoque negativo
    })
    void deveRejeitarDadosInvalidos(String nome, String preco, String estoque) throws Exception {
        mockMvc.perform(post("/produtos/salvar")
                        .param("nome", nome)
                        .param("preco", preco)
                        .param("quantidadeEstoque", estoque)
                        .param("ativo", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("produto/formulario"));

        Assertions.assertEquals(0, produtoRepository.count());
    }

    @Test
    @DisplayName("POST /produtos/salvar - Deve rejeitar codigo de barras duplicado")
    void deveRejeitarCodigoBarrasDuplicado() throws Exception {
        produtoRepository.save(new Produto(null, "Existente", "desc", new BigDecimal("5.00"), 10, "DUPLIC", true));

        mockMvc.perform(post("/produtos/salvar")
                        .param("nome", "Novo Produto")
                        .param("preco", "10.00")
                        .param("quantidadeEstoque", "50")
                        .param("codigoBarras", "DUPLIC")
                        .param("ativo", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("GET /produtos/editar/{id} - Deve exibir formulario de edicao")
    void deveExibirFormularioDeEdicao() throws Exception {
        Produto salvo = produtoRepository.save(
                new Produto(null, "Editar", "desc", new BigDecimal("10.00"), 20, "EDIT01", true));

        mockMvc.perform(get("/produtos/editar/" + salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("produto/formulario"))
                .andExpect(model().attribute("acao", "Atualizar"));
    }

    @Test
    @DisplayName("GET /produtos/editar/{id} - Deve redirecionar para ID inexistente")
    void deveRedirecionarParaIdInexistente() throws Exception {
        mockMvc.perform(get("/produtos/editar/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("POST /produtos/atualizar/{id} - Deve atualizar produto com sucesso")
    void deveAtualizarProdutoComSucesso() throws Exception {
        Produto salvo = produtoRepository.save(
                new Produto(null, "Antes", "desc", new BigDecimal("5.00"), 10, "UPD01", true));

        mockMvc.perform(post("/produtos/atualizar/" + salvo.getId())
                        .param("nome", "Depois")
                        .param("descricao", "Nova descricao")
                        .param("preco", "15.00")
                        .param("quantidadeEstoque", "50")
                        .param("codigoBarras", "UPD01")
                        .param("ativo", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"));

        Produto atualizado = produtoRepository.findById(salvo.getId()).orElseThrow();
        Assertions.assertEquals("Depois", atualizado.getNome());
    }

    @Test
    @DisplayName("GET /produtos/excluir/{id} - Deve excluir produto existente")
    void deveExcluirProdutoExistente() throws Exception {
        Produto salvo = produtoRepository.save(
                new Produto(null, "Excluir", "desc", new BigDecimal("5.00"), 10, "DEL01", true));

        mockMvc.perform(get("/produtos/excluir/" + salvo.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));

        Assertions.assertEquals(0, produtoRepository.count());
    }

    @Test
    @DisplayName("GET /produtos/excluir/{id} - Deve tratar exclusao de ID inexistente")
    void deveTratarExclusaoDeIdInexistente() throws Exception {
        mockMvc.perform(get("/produtos/excluir/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }
}
