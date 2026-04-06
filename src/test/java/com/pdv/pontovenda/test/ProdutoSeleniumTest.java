package com.pdv.pontovenda.test;

import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.page.ProdutoFormularioPage;
import com.pdv.pontovenda.page.ProdutoListagemPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes Selenium para o modulo de Produtos.
 * Utiliza o padrao Page Object Model (POM) para encapsular interacoes com a interface.
 * Inclui testes parametrizados, testes negativos e validacao de fluxos completos.
 */
@DisplayName("Selenium - CRUD de Produtos")
class ProdutoSeleniumTest extends BaseSeleniumTest {

    @Nested
    @DisplayName("Navegacao")
    class Navegacao {

        @Test
        @DisplayName("Deve navegar para listagem de produtos")
        void deveNavegarParaListagemDeProdutos() {
            navegarPara("/produtos");

            ProdutoListagemPage pagina = new ProdutoListagemPage(driver);
            assertThat(pagina.getTituloPagina()).contains("Produto");
        }

        @Test
        @DisplayName("Deve exibir mensagem quando nao ha produtos")
        void deveExibirMensagemSemRegistros() {
            navegarPara("/produtos");

            ProdutoListagemPage pagina = new ProdutoListagemPage(driver);
            assertThat(pagina.mensagemSemRegistrosVisivel()).isTrue();
        }

        @Test
        @DisplayName("Deve navegar para formulario de cadastro e voltar")
        void deveNavegarParaFormularioEVoltar() {
            navegarPara("/produtos");

            ProdutoListagemPage listagem = new ProdutoListagemPage(driver);
            ProdutoFormularioPage formulario = listagem.clicarNovoProduto();

            assertThat(formulario.getUrlAtual()).contains("/produtos/novo");

            ProdutoListagemPage retorno = formulario.clicarCancelar();
            assertThat(retorno.getUrlAtual()).contains("/produtos");
        }
    }

    @Nested
    @DisplayName("Cadastro")
    class Cadastro {

        @Test
        @DisplayName("Deve cadastrar um produto com dados validos e exibir na tabela")
        void deveCadastrarProdutoComDadosValidos() {
            navegarPara("/produtos/novo");

            ProdutoFormularioPage formulario = new ProdutoFormularioPage(driver);
            ProdutoListagemPage listagem = formulario
                    .preencherFormulario("Arroz Integral", "Arroz integral 1kg", "8.90", "100", "7891000001", true)
                    .clicarSalvar();

            assertThat(listagem.alertaSucessoVisivel()).isTrue();
            assertThat(listagem.contarLinhas()).isEqualTo(1);
            assertThat(listagem.getTextoCelula(0, 1)).isEqualTo("Arroz Integral");
        }

        @ParameterizedTest(name = "Cenario {index}: nome={0}, preco={1}, estoque={2}, codigo={3}")
        @DisplayName("Deve cadastrar produtos com diferentes dados")
        @CsvSource({
                "Feijao Preto, 7.50, 200, 7891000002",
                "Oleo de Soja, 5.99, 80, 7891000003",
                "Cafe Premium, 25.90, 40, 7891000004"
        })
        void deveCadastrarProdutosComDiferentesDados(String nome, String preco, String estoque, String codigo) {
            navegarPara("/produtos/novo");

            ProdutoFormularioPage formulario = new ProdutoFormularioPage(driver);
            ProdutoListagemPage listagem = formulario
                    .preencherFormulario(nome, "Descricao do " + nome, preco, estoque, codigo, true)
                    .clicarSalvar();

            assertThat(listagem.alertaSucessoVisivel()).isTrue();
            assertThat(listagem.contarLinhas()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Deve cadastrar produto sem codigo de barras")
        void deveCadastrarProdutoSemCodigoBarras() {
            navegarPara("/produtos/novo");

            ProdutoFormularioPage formulario = new ProdutoFormularioPage(driver);
            ProdutoListagemPage listagem = formulario
                    .preencherNome("Produto Sem Codigo")
                    .preencherDescricao("Produto sem codigo de barras")
                    .preencherPreco("3.50")
                    .preencherEstoque("10")
                    .definirAtivo(true)
                    .clicarSalvar();

            assertThat(listagem.alertaSucessoVisivel()).isTrue();
            assertThat(listagem.contarLinhas()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Edicao")
    class Edicao {

        @Test
        @DisplayName("Deve editar um produto existente e verificar alteracao")
        void deveEditarProdutoExistente() {
            produtoRepository.save(new Produto(null, "Original", "desc", new BigDecimal("10.00"), 50, "ORIG01", true));

            navegarPara("/produtos");
            ProdutoListagemPage listagem = new ProdutoListagemPage(driver);

            ProdutoFormularioPage formulario = listagem.clicarEditar(0);

            assertThat(formulario.getValorNome()).isEqualTo("Original");

            ProdutoListagemPage retorno = formulario
                    .preencherNome("Produto Atualizado")
                    .preencherPreco("15.50")
                    .clicarSalvar();

            assertThat(retorno.alertaSucessoVisivel()).isTrue();
            assertThat(retorno.getTextoCelula(0, 1)).isEqualTo("Produto Atualizado");
        }
    }

    @Nested
    @DisplayName("Exclusao")
    class Exclusao {

        @Test
        @DisplayName("Deve excluir um produto e confirmar remocao da tabela")
        void deveExcluirProdutoComSucesso() {
            produtoRepository.save(new Produto(null, "Excluir", "desc", new BigDecimal("5.00"), 10, "DEL01", true));

            navegarPara("/produtos");
            ProdutoListagemPage listagem = new ProdutoListagemPage(driver);

            assertThat(listagem.contarLinhas()).isEqualTo(1);

            listagem.clicarExcluir(0).confirmarExclusao();

            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            navegarPara("/produtos");
            listagem = new ProdutoListagemPage(driver);

            assertThat(listagem.contarLinhas()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Testes Negativos - Validacao de Erros")
    class TestesNegativos {

        @Test
        @DisplayName("Deve exibir erro ao cadastrar com codigo de barras duplicado")
        void deveExibirErroComCodigoBarrasDuplicado() {
            produtoRepository.save(new Produto(null, "Existente", "desc", new BigDecimal("5.00"), 10, "DUPLIC", true));

            navegarPara("/produtos/novo");
            ProdutoFormularioPage formulario = new ProdutoFormularioPage(driver);
            formulario.preencherFormulario("Novo Produto", "desc", "10.00", "20", "DUPLIC", true)
                    .clicarSalvarComErro();

            assertThat(formulario.alertaErroFormVisivel()).isTrue();
            assertThat(formulario.getTextoAlertaErroForm()).contains("DUPLIC");
        }

        @ParameterizedTest(name = "Cenario negativo {index}: nome={0}, preco={1}, estoque={2}")
        @DisplayName("Deve rejeitar dados invalidos no formulario de produto")
        @CsvSource({
                "'', 8.90, 100",     // nome vazio
                "A, 8.90, 100"       // nome muito curto
        })
        void deveRejeitarDadosInvalidos(String nome, String preco, String estoque) {
            navegarPara("/produtos/novo");
            ProdutoFormularioPage formulario = new ProdutoFormularioPage(driver);
            formulario.preencherNome(nome)
                    .preencherPreco(preco)
                    .preencherEstoque(estoque)
                    .clicarSalvarComErro();

            assertThat(formulario.getUrlAtual()).containsAnyOf("/produtos/salvar", "/produtos/novo");
        }
    }

    @Nested
    @DisplayName("Fluxo Completo")
    class FluxoCompleto {

        @Test
        @DisplayName("Deve executar fluxo completo: cadastrar -> listar -> editar -> excluir")
        void deveExecutarFluxoCompletoCRUD() {
            // 1. Cadastrar
            navegarPara("/produtos/novo");
            ProdutoFormularioPage formulario = new ProdutoFormularioPage(driver);
            ProdutoListagemPage listagem = formulario
                    .preencherFormulario("CRUD Produto", "Teste fluxo completo", "19.90", "75", "CRUD01", true)
                    .clicarSalvar();

            assertThat(listagem.alertaSucessoVisivel()).isTrue();
            assertThat(listagem.contarLinhas()).isEqualTo(1);

            // 2. Editar
            formulario = listagem.clicarEditar(0);
            listagem = formulario
                    .preencherNome("CRUD Produto Editado")
                    .preencherPreco("24.90")
                    .clicarSalvar();

            assertThat(listagem.alertaSucessoVisivel()).isTrue();
            assertThat(listagem.getTextoCelula(0, 1)).isEqualTo("CRUD Produto Editado");

            // 3. Excluir
            listagem.clicarExcluir(0).confirmarExclusao();

            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            navegarPara("/produtos");
            listagem = new ProdutoListagemPage(driver);
            assertThat(listagem.contarLinhas()).isEqualTo(0);
        }
    }
}
