package com.pdv.pontovenda.test;

import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integracao para UsuarioController.
 * Validam o fluxo completo: controller -> service -> repository.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UsuarioController - Testes de Integracao")
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /usuarios - Deve exibir pagina de listagem")
    void deveExibirPaginaDeListagem() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/listagem"))
                .andExpect(model().attributeExists("usuarios"));
    }

    @Test
    @DisplayName("GET /usuarios - Deve listar usuarios cadastrados")
    void deveListarUsuariosCadastrados() throws Exception {
        usuarioRepository.save(new Usuario(null, "Admin", "admin@pdv.com", "admin123", "ADMIN", true));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("usuarios", hasSize(1)));
    }

    @Test
    @DisplayName("GET /usuarios/novo - Deve exibir formulario de cadastro")
    void deveExibirFormularioDeCadastro() throws Exception {
        mockMvc.perform(get("/usuarios/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/formulario"))
                .andExpect(model().attributeExists("usuario", "perfis", "acao"));
    }

    @Test
    @DisplayName("POST /usuarios/salvar - Deve cadastrar usuario valido e redirecionar")
    void deveCadastrarUsuarioValidoERedirecionar() throws Exception {
        mockMvc.perform(post("/usuarios/salvar")
                        .param("nome", "Novo Usuario")
                        .param("email", "novo@pdv.com")
                        .param("senha", "senha123")
                        .param("perfil", "OPERADOR")
                        .param("ativo", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attributeExists("mensagemSucesso"));

        Assertions.assertEquals(1, usuarioRepository.count());
    }

    @ParameterizedTest(name = "Teste {index}: nome={0}, email={1}, senha={2}, perfil={3}")
    @DisplayName("POST /usuarios/salvar - Deve rejeitar dados invalidos")
    @CsvSource({
            "'', email@pdv.com, senha123, ADMIN",         // nome vazio
            "Nome, '', senha123, ADMIN",                   // email vazio
            "Nome, email@pdv.com, '', ADMIN",              // senha vazia
            "Nome, email@pdv.com, senha123, ''",           // perfil vazio
            "AB, email@pdv.com, senha123, ADMIN",          // nome curto
            "Nome, emailinvalido, senha123, ADMIN",        // email invalido
            "Nome, email@pdv.com, abc, ADMIN"              // senha curta
    })
    void deveRejeitarDadosInvalidos(String nome, String email, String senha, String perfil) throws Exception {
        mockMvc.perform(post("/usuarios/salvar")
                        .param("nome", nome)
                        .param("email", email)
                        .param("senha", senha)
                        .param("perfil", perfil)
                        .param("ativo", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/formulario"));

        Assertions.assertEquals(0, usuarioRepository.count());
    }

    @Test
    @DisplayName("POST /usuarios/salvar - Deve rejeitar e-mail duplicado")
    void deveRejeitarEmailDuplicado() throws Exception {
        usuarioRepository.save(new Usuario(null, "Existente", "dup@pdv.com", "senha123", "ADMIN", true));

        mockMvc.perform(post("/usuarios/salvar")
                        .param("nome", "Novo")
                        .param("email", "dup@pdv.com")
                        .param("senha", "senha123")
                        .param("perfil", "OPERADOR")
                        .param("ativo", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("GET /usuarios/editar/{id} - Deve exibir formulario de edicao")
    void deveExibirFormularioDeEdicao() throws Exception {
        Usuario salvo = usuarioRepository.save(new Usuario(null, "Editar", "edit@pdv.com", "senha123", "ADMIN", true));

        mockMvc.perform(get("/usuarios/editar/" + salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/formulario"))
                .andExpect(model().attribute("acao", "Atualizar"));
    }

    @Test
    @DisplayName("GET /usuarios/editar/{id} - Deve redirecionar para ID inexistente")
    void deveRedirecionarParaIdInexistente() throws Exception {
        mockMvc.perform(get("/usuarios/editar/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("POST /usuarios/atualizar/{id} - Deve atualizar usuario com sucesso")
    void deveAtualizarUsuarioComSucesso() throws Exception {
        Usuario salvo = usuarioRepository.save(new Usuario(null, "Antes", "antes@pdv.com", "senha123", "ADMIN", true));

        mockMvc.perform(post("/usuarios/atualizar/" + salvo.getId())
                        .param("nome", "Depois")
                        .param("email", "depois@pdv.com")
                        .param("senha", "novasenha")
                        .param("perfil", "OPERADOR")
                        .param("ativo", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        Usuario atualizado = usuarioRepository.findById(salvo.getId()).orElseThrow();
        Assertions.assertEquals("Depois", atualizado.getNome());
        Assertions.assertEquals("depois@pdv.com", atualizado.getEmail());
    }

    @Test
    @DisplayName("GET /usuarios/excluir/{id} - Deve excluir usuario existente")
    void deveExcluirUsuarioExistente() throws Exception {
        Usuario salvo = usuarioRepository.save(new Usuario(null, "Excluir", "excluir@pdv.com", "senha123", "ADMIN", true));

        mockMvc.perform(get("/usuarios/excluir/" + salvo.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attributeExists("mensagemSucesso"));

        Assertions.assertEquals(0, usuarioRepository.count());
    }

    @Test
    @DisplayName("GET /usuarios/excluir/{id} - Deve tratar exclusao de ID inexistente")
    void deveTratarExclusaoDeIdInexistente() throws Exception {
        mockMvc.perform(get("/usuarios/excluir/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attributeExists("mensagemErro"));
    }
}
