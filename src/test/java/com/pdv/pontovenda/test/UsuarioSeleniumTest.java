package com.pdv.pontovenda.test;

import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.page.UsuarioFormularioPage;
import com.pdv.pontovenda.page.UsuarioListagemPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes Selenium para o modulo de Usuarios.
 * Utiliza o padrao Page Object Model (POM) para encapsular interacoes com a interface.
 * Inclui testes parametrizados, testes negativos e validacao de fluxos completos.
 */
@DisplayName("Selenium - CRUD de Usuarios")
class UsuarioSeleniumTest extends BaseSeleniumTest {

    @Nested
    @DisplayName("Navegacao")
    class Navegacao {

        @Test
        @DisplayName("Deve acessar a pagina inicial do sistema")
        void deveAcessarPaginaInicial() {
            navegarPara("/");
            assertThat(driver.getTitle()).contains("PDV");
        }

        @Test
        @DisplayName("Deve navegar da home para listagem de usuarios")
        void deveNavegarParaListagemDeUsuarios() {
            navegarPara("/usuarios");

            UsuarioListagemPage pagina = new UsuarioListagemPage(driver);
            assertThat(pagina.getTituloPagina()).contains("Usuario");
        }

        @Test
        @DisplayName("Deve exibir mensagem quando nao ha usuarios cadastrados")
        void deveExibirMensagemSemRegistros() {
            navegarPara("/usuarios");

            UsuarioListagemPage pagina = new UsuarioListagemPage(driver);
            assertThat(pagina.mensagemSemRegistrosVisivel()).isTrue();
        }

        @Test
        @DisplayName("Deve navegar para formulario de cadastro e voltar")
        void deveNavegarParaFormularioEVoltar() {
            navegarPara("/usuarios");

            UsuarioListagemPage listagem = new UsuarioListagemPage(driver);
            UsuarioFormularioPage formulario = listagem.clicarNovoUsuario();

            assertThat(formulario.getUrlAtual()).contains("/usuarios/novo");

            UsuarioListagemPage retorno = formulario.clicarCancelar();
            assertThat(retorno.getUrlAtual()).contains("/usuarios");
        }
    }

    @Nested
    @DisplayName("Cadastro")
    class Cadastro {

        @Test
        @DisplayName("Deve cadastrar um usuario com dados validos e exibir na tabela")
        void deveCadastrarUsuarioComDadosValidos() {
            navegarPara("/usuarios/novo");

            UsuarioFormularioPage formulario = new UsuarioFormularioPage(driver);
            UsuarioListagemPage listagem = formulario
                    .preencherFormulario("Carlos Silva", "carlos@pdv.com", "senha123", "OPERADOR", true)
                    .clicarSalvar();

            assertThat(listagem.alertaSucessoVisivel()).isTrue();
            assertThat(listagem.contarLinhas()).isEqualTo(1);
            assertThat(listagem.getTextoCelula(0, 1)).isEqualTo("Carlos Silva");
            assertThat(listagem.getTextoCelula(0, 2)).isEqualTo("carlos@pdv.com");
        }

        @ParameterizedTest(name = "Cenario {index}: nome={0}, email={1}, senha={2}, perfil={3}")
        @DisplayName("Deve cadastrar usuarios com diferentes perfis e dados")
        @CsvSource({
                "Admin Master, admin@teste.com, admin123, ADMIN",
                "Operador PDV, operador@teste.com, oper123, OPERADOR"
        })
        void deveCadastrarUsuariosComDiferentesPerfis(String nome, String email, String senha, String perfil) {
            navegarPara("/usuarios/novo");

            UsuarioFormularioPage formulario = new UsuarioFormularioPage(driver);
            UsuarioListagemPage listagem = formulario
                    .preencherFormulario(nome, email, senha, perfil, true)
                    .clicarSalvar();

            assertThat(listagem.alertaSucessoVisivel()).isTrue();
            assertThat(listagem.contarLinhas()).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Edicao")
    class Edicao {

        @Test
        @DisplayName("Deve editar um usuario existente e verificar alteracao")
        void deveEditarUsuarioExistente() {
            usuarioRepository.save(new Usuario(null, "Original", "original@pdv.com", "senha123", "OPERADOR", true));

            navegarPara("/usuarios");
            UsuarioListagemPage listagem = new UsuarioListagemPage(driver);

            UsuarioFormularioPage formulario = listagem.clicarEditar(0);

            // Verifica que os dados foram carregados
            assertThat(formulario.getValorNome()).isEqualTo("Original");
            assertThat(formulario.getValorEmail()).isEqualTo("original@pdv.com");

            // Altera os dados (senha precisa ser preenchida pois campo password nao mantem valor)
            UsuarioListagemPage retorno = formulario
                    .preencherNome("Atualizado")
                    .preencherEmail("atualizado@pdv.com")
                    .preencherSenha("senha123")
                    .clicarSalvar();

            assertThat(retorno.alertaSucessoVisivel()).isTrue();
            assertThat(retorno.getTextoCelula(0, 1)).isEqualTo("Atualizado");
            assertThat(retorno.getTextoCelula(0, 2)).isEqualTo("atualizado@pdv.com");
        }
    }

    @Nested
    @DisplayName("Exclusao")
    class Exclusao {

        @Test
        @DisplayName("Deve excluir um usuario e confirmar remocao da tabela")
        void deveExcluirUsuarioComSucesso() {
            usuarioRepository.save(new Usuario(null, "Excluir", "excluir@pdv.com", "senha123", "OPERADOR", true));

            navegarPara("/usuarios");
            UsuarioListagemPage listagem = new UsuarioListagemPage(driver);

            assertThat(listagem.contarLinhas()).isEqualTo(1);

            listagem.clicarExcluir(0).confirmarExclusao();

            // Aguardar reload apos exclusao
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            navegarPara("/usuarios");
            listagem = new UsuarioListagemPage(driver);

            assertThat(listagem.contarLinhas()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Testes Negativos - Validacao de Erros")
    class TestesNegativos {

        @Test
        @DisplayName("Deve exibir erro ao cadastrar com e-mail duplicado")
        void deveExibirErroComEmailDuplicado() {
            usuarioRepository.save(new Usuario(null, "Existente", "dup@pdv.com", "senha123", "ADMIN", true));

            navegarPara("/usuarios/novo");
            UsuarioFormularioPage formulario = new UsuarioFormularioPage(driver);
            formulario.preencherFormulario("Novo", "dup@pdv.com", "senha123", "OPERADOR", true)
                    .clicarSalvarComErro();

            assertThat(formulario.alertaErroFormVisivel()).isTrue();
            assertThat(formulario.getTextoAlertaErroForm()).contains("dup@pdv.com");
        }

        @ParameterizedTest(name = "Cenario negativo {index}: nome={0}, email={1}, senha={2}")
        @DisplayName("Deve rejeitar dados invalidos no formulario")
        @CsvSource({
                "'', email@pdv.com, senha123",     // nome vazio
                "Nome Valido, '', senha123",         // email vazio
                "Nome Valido, email@pdv.com, ''"     // senha vazia
        })
        void deveRejeitarDadosInvalidos(String nome, String email, String senha) {
            navegarPara("/usuarios/novo");
            UsuarioFormularioPage formulario = new UsuarioFormularioPage(driver);
            formulario.preencherNome(nome)
                    .preencherEmail(email)
                    .preencherSenha(senha)
                    .selecionarPerfil("ADMIN")
                    .clicarSalvarComErro();

            // Deve permanecer na pagina de formulario (URL contem /usuarios)
            assertThat(formulario.getUrlAtual()).containsAnyOf("/usuarios/salvar", "/usuarios/novo");
        }

        @Test
        @DisplayName("Deve exibir erro ao editar com e-mail de outro usuario")
        void deveExibirErroAoEditarComEmailDeOutroUsuario() {
            usuarioRepository.save(new Usuario(null, "User A", "a@pdv.com", "senha123", "ADMIN", true));
            usuarioRepository.save(new Usuario(null, "User B", "b@pdv.com", "senha123", "OPERADOR", true));

            navegarPara("/usuarios");
            UsuarioListagemPage listagem = new UsuarioListagemPage(driver);

            // Edita o segundo usuario tentando usar o email do primeiro
            // Senha precisa ser preenchida pois campo password nao mantem valor no browser
            UsuarioFormularioPage formulario = listagem.clicarEditar(1);
            formulario.preencherSenha("senha123")
                    .preencherEmail("a@pdv.com")
                    .clicarSalvarComErro();

            assertThat(formulario.alertaErroFormVisivel()).isTrue();
        }
    }

    @Nested
    @DisplayName("Fluxo Completo")
    class FluxoCompleto {

        @Test
        @DisplayName("Deve executar fluxo completo: cadastrar -> listar -> editar -> excluir")
        void deveExecutarFluxoCompletoCRUD() {
            // 1. Cadastrar
            navegarPara("/usuarios/novo");
            UsuarioFormularioPage formulario = new UsuarioFormularioPage(driver);
            UsuarioListagemPage listagem = formulario
                    .preencherFormulario("CRUD User", "crud@pdv.com", "senha123", "ADMIN", true)
                    .clicarSalvar();

            assertThat(listagem.alertaSucessoVisivel()).isTrue();
            assertThat(listagem.contarLinhas()).isEqualTo(1);

            // 2. Editar (senha precisa ser repreenchida pois campo password nao mantem valor)
            formulario = listagem.clicarEditar(0);
            listagem = formulario
                    .preencherNome("CRUD User Editado")
                    .preencherSenha("senha123")
                    .clicarSalvar();

            assertThat(listagem.alertaSucessoVisivel()).isTrue();
            assertThat(listagem.getTextoCelula(0, 1)).isEqualTo("CRUD User Editado");

            // 3. Excluir
            listagem.clicarExcluir(0).confirmarExclusao();

            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            navegarPara("/usuarios");
            listagem = new UsuarioListagemPage(driver);
            assertThat(listagem.contarLinhas()).isEqualTo(0);
        }
    }
}
