package com.pdv.pontovenda.test;

import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import com.pdv.pontovenda.repository.UsuarioRepository;
import com.pdv.pontovenda.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para UsuarioService.
 * Validam regras de negocio isoladamente usando Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Testes Unitarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioValido;

    @BeforeEach
    void setUp() {
        usuarioValido = new Usuario(null, "Teste User", "teste@pdv.com", "senha123", "OPERADOR", true);
    }

    @Nested
    @DisplayName("Listar Todos")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha usuarios")
        void deveRetornarListaVazia() {
            when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

            List<Usuario> resultado = usuarioService.listarTodos();

            assertThat(resultado).isEmpty();
            verify(usuarioRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista com usuarios cadastrados")
        void deveRetornarListaComUsuarios() {
            when(usuarioRepository.findAll()).thenReturn(List.of(usuarioValido));

            List<Usuario> resultado = usuarioService.listarTodos();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNome()).isEqualTo("Teste User");
        }
    }

    @Nested
    @DisplayName("Buscar por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar usuario quando ID existe")
        void deveRetornarUsuarioQuandoIdExiste() {
            usuarioValido.setId(1L);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

            Usuario resultado = usuarioService.buscarPorId(1L);

            assertThat(resultado.getNome()).isEqualTo("Teste User");
        }

        @Test
        @DisplayName("Deve lancar excecao quando ID nao existe")
        void deveLancarExcecaoQuandoIdNaoExiste() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.buscarPorId(999L))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("Salvar")
    class Salvar {

        @Test
        @DisplayName("Deve salvar usuario com dados validos")
        void deveSalvarUsuarioComDadosValidos() {
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hasheado");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioValido);

            Usuario resultado = usuarioService.salvar(usuarioValido);

            assertThat(resultado.getNome()).isEqualTo("Teste User");
            verify(usuarioRepository).save(usuarioValido);
        }

        @Test
        @DisplayName("Deve lancar excecao quando e-mail ja cadastrado")
        void deveLancarExcecaoQuandoEmailDuplicado() {
            when(usuarioRepository.existsByEmail("teste@pdv.com")).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.salvar(usuarioValido))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("teste@pdv.com");

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Atualizar")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar usuario existente com sucesso")
        void deveAtualizarUsuarioExistente() {
            usuarioValido.setId(1L);
            Usuario atualizado = new Usuario(null, "Nome Atualizado", "novo@pdv.com", "novasenha", "ADMIN", true);

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(usuarioRepository.existsByEmailAndIdNot("novo@pdv.com", 1L)).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hasheado");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioValido);

            Usuario resultado = usuarioService.atualizar(1L, atualizado);

            assertThat(resultado).isNotNull();
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lancar excecao ao atualizar com e-mail de outro usuario")
        void deveLancarExcecaoAoAtualizarComEmailDuplicado() {
            usuarioValido.setId(1L);
            Usuario atualizado = new Usuario(null, "Teste", "existente@pdv.com", "senha", "ADMIN", true);

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
            when(usuarioRepository.existsByEmailAndIdNot("existente@pdv.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.atualizar(1L, atualizado))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("existente@pdv.com");
        }

        @Test
        @DisplayName("Deve lancar excecao ao atualizar usuario inexistente")
        void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.atualizar(999L, usuarioValido))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Excluir")
    class Excluir {

        @Test
        @DisplayName("Deve excluir usuario existente")
        void deveExcluirUsuarioExistente() {
            usuarioValido.setId(1L);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

            usuarioService.excluir(1L);

            verify(usuarioRepository).delete(usuarioValido);
        }

        @Test
        @DisplayName("Deve lancar excecao ao excluir usuario inexistente")
        void deveLancarExcecaoAoExcluirUsuarioInexistente() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.excluir(999L))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }
}
