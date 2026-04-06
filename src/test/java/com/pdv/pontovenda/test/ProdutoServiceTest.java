package com.pdv.pontovenda.test;

import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import com.pdv.pontovenda.repository.ProdutoRepository;
import com.pdv.pontovenda.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para ProdutoService.
 * Validam regras de negocio isoladamente usando Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService - Testes Unitarios")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produtoValido;

    @BeforeEach
    void setUp() {
        produtoValido = new Produto(null, "Arroz 1kg", "Arroz tipo 1", new BigDecimal("8.90"), 100, "7890001", true);
    }

    @Nested
    @DisplayName("Listar Todos")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha produtos")
        void deveRetornarListaVazia() {
            when(produtoRepository.findAll()).thenReturn(Collections.emptyList());

            List<Produto> resultado = produtoService.listarTodos();

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista com produtos cadastrados")
        void deveRetornarListaComProdutos() {
            when(produtoRepository.findAll()).thenReturn(List.of(produtoValido));

            List<Produto> resultado = produtoService.listarTodos();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNome()).isEqualTo("Arroz 1kg");
        }
    }

    @Nested
    @DisplayName("Buscar por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar produto quando ID existe")
        void deveRetornarProdutoQuandoIdExiste() {
            produtoValido.setId(1L);
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoValido));

            Produto resultado = produtoService.buscarPorId(1L);

            assertThat(resultado.getNome()).isEqualTo("Arroz 1kg");
        }

        @Test
        @DisplayName("Deve lancar excecao quando ID nao existe")
        void deveLancarExcecaoQuandoIdNaoExiste() {
            when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.buscarPorId(999L))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("Salvar")
    class Salvar {

        @Test
        @DisplayName("Deve salvar produto com dados validos")
        void deveSalvarProdutoComDadosValidos() {
            when(produtoRepository.existsByCodigoBarras(anyString())).thenReturn(false);
            when(produtoRepository.save(any(Produto.class))).thenReturn(produtoValido);

            Produto resultado = produtoService.salvar(produtoValido);

            assertThat(resultado.getNome()).isEqualTo("Arroz 1kg");
            verify(produtoRepository).save(produtoValido);
        }

        @Test
        @DisplayName("Deve salvar produto sem codigo de barras")
        void deveSalvarProdutoSemCodigoBarras() {
            produtoValido.setCodigoBarras(null);
            when(produtoRepository.save(any(Produto.class))).thenReturn(produtoValido);

            Produto resultado = produtoService.salvar(produtoValido);

            assertThat(resultado).isNotNull();
            verify(produtoRepository, never()).existsByCodigoBarras(anyString());
        }

        @Test
        @DisplayName("Deve salvar produto com codigo de barras em branco")
        void deveSalvarProdutoComCodigoBarrasEmBranco() {
            produtoValido.setCodigoBarras("   ");
            when(produtoRepository.save(any(Produto.class))).thenReturn(produtoValido);

            Produto resultado = produtoService.salvar(produtoValido);

            assertThat(resultado).isNotNull();
            verify(produtoRepository, never()).existsByCodigoBarras(anyString());
        }

        @Test
        @DisplayName("Deve lancar excecao quando codigo de barras ja cadastrado")
        void deveLancarExcecaoQuandoCodigoBarrasDuplicado() {
            when(produtoRepository.existsByCodigoBarras("7890001")).thenReturn(true);

            assertThatThrownBy(() -> produtoService.salvar(produtoValido))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("7890001");

            verify(produtoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Atualizar")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar produto existente com sucesso")
        void deveAtualizarProdutoExistente() {
            produtoValido.setId(1L);
            Produto atualizado = new Produto(null, "Arroz Premium", "Arroz premium", new BigDecimal("12.90"), 50, "7890002", true);

            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoValido));
            when(produtoRepository.existsByCodigoBarrasAndIdNot("7890002", 1L)).thenReturn(false);
            when(produtoRepository.save(any(Produto.class))).thenReturn(produtoValido);

            Produto resultado = produtoService.atualizar(1L, atualizado);

            assertThat(resultado).isNotNull();
            verify(produtoRepository).save(any(Produto.class));
        }

        @Test
        @DisplayName("Deve lancar excecao ao atualizar com codigo de barras de outro produto")
        void deveLancarExcecaoAoAtualizarComCodigoBarrasDuplicado() {
            produtoValido.setId(1L);
            Produto atualizado = new Produto(null, "Arroz", "desc", new BigDecimal("10"), 10, "EXISTENTE", true);

            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoValido));
            when(produtoRepository.existsByCodigoBarrasAndIdNot("EXISTENTE", 1L)).thenReturn(true);

            assertThatThrownBy(() -> produtoService.atualizar(1L, atualizado))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("EXISTENTE");
        }

        @Test
        @DisplayName("Deve lancar excecao ao atualizar produto inexistente")
        void deveLancarExcecaoAoAtualizarProdutoInexistente() {
            when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.atualizar(999L, produtoValido))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Excluir")
    class Excluir {

        @Test
        @DisplayName("Deve excluir produto existente")
        void deveExcluirProdutoExistente() {
            produtoValido.setId(1L);
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoValido));

            produtoService.excluir(1L);

            verify(produtoRepository).delete(produtoValido);
        }

        @Test
        @DisplayName("Deve lancar excecao ao excluir produto inexistente")
        void deveLancarExcecaoAoExcluirProdutoInexistente() {
            when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.excluir(999L))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }
}
