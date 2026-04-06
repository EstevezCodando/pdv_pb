package com.pdv.pontovenda.service;

import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import com.pdv.pontovenda.repository.ProdutoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Service responsavel pelas regras de negocio relacionadas a Produto.
 * Valida duplicidade de codigo de barras e garante consistencia nas operacoes CRUD.
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Produto> listarAtivos() {
        return produtoRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public long contarTodos() {
        return produtoRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarAtivos() {
        return produtoRepository.countByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    @Transactional(readOnly = true)
    public Produto buscarAtivoPorId(Long id) {
        Produto produto = buscarPorId(id);
        if (Boolean.FALSE.equals(produto.getAtivo())) {
            throw new RegraDeNegocioException("O produto informado esta inativo e nao pode participar de uma venda.");
        }
        return produto;
    }

    @Transactional
    public Produto salvar(Produto produto) {
        validarCodigoBarrasUnico(produto);
        try {
            Produto produtoSalvo = produtoRepository.save(produto);
            produtoRepository.flush();
            return produtoSalvo;
        } catch (DataIntegrityViolationException ex) {
            throw traduzirViolacaoDeIntegridade(produto.getCodigoBarras());
        }
    }

    @Transactional
    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto existente = buscarPorId(id);

        validarCodigoBarrasUnicoParaAtualizacao(produtoAtualizado.getCodigoBarras(), id);
        aplicarAlteracoes(produtoAtualizado, existente);

        try {
            Produto produtoSalvo = produtoRepository.save(existente);
            produtoRepository.flush();
            return produtoSalvo;
        } catch (DataIntegrityViolationException ex) {
            throw traduzirViolacaoDeIntegridade(existente.getCodigoBarras());
        }
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }

    @Transactional
    public void baixarEstoque(Produto produto, int quantidade) {
        int quantidadeAtual = Objects.requireNonNullElse(produto.getQuantidadeEstoque(), 0);
        produto.setQuantidadeEstoque(quantidadeAtual - quantidade);
        produtoRepository.save(produto);
    }

    private void aplicarAlteracoes(Produto origem, Produto destino) {
        destino.setNome(origem.getNome());
        destino.setDescricao(origem.getDescricao());
        destino.setPreco(origem.getPreco());
        destino.setQuantidadeEstoque(origem.getQuantidadeEstoque());
        destino.setCodigoBarras(origem.getCodigoBarras());
        destino.setAtivo(origem.getAtivo());
    }

    private void validarCodigoBarrasUnico(Produto produto) {
        String codigo = produto.getCodigoBarras();
        if (codigoBarrasPreenchido(codigo) && produtoRepository.existsByCodigoBarras(codigo)) {
            throw traduzirViolacaoDeIntegridade(codigo);
        }
    }

    private void validarCodigoBarrasUnicoParaAtualizacao(String codigo, Long idAtual) {
        if (codigoBarrasPreenchido(codigo) && produtoRepository.existsByCodigoBarrasAndIdNot(codigo, idAtual)) {
            throw traduzirViolacaoDeIntegridade(codigo);
        }
    }

    private boolean codigoBarrasPreenchido(String codigo) {
        return StringUtils.hasText(codigo);
    }

    private RegraDeNegocioException traduzirViolacaoDeIntegridade(String codigoBarras) {
        if (!StringUtils.hasText(codigoBarras)) {
            return new RegraDeNegocioException("Nao foi possivel persistir o produto com os dados informados.");
        }
        return new RegraDeNegocioException("Ja existe um produto com o codigo de barras: " + codigoBarras);
    }
}
