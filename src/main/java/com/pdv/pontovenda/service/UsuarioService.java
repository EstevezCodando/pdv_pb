package com.pdv.pontovenda.service;

import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import com.pdv.pontovenda.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Service responsavel pelas regras de negocio relacionadas a Usuario.
 * Valida duplicidade de e-mail e garante consistencia nas operacoes CRUD.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarAtivos() {
        return usuarioRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public long contarTodos() {
        return usuarioRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarAtivos() {
        return usuarioRepository.countByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarAtivoPorId(Long id) {
        Usuario usuario = buscarPorId(id);
        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new RegraDeNegocioException("O usuario informado esta inativo e nao pode operar no caixa.");
        }
        return usuario;
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        validarEmailUnico(usuario.getEmail());
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        try {
            Usuario usuarioSalvo = usuarioRepository.save(usuario);
            usuarioRepository.flush();
            return usuarioSalvo;
        } catch (DataIntegrityViolationException ex) {
            throw traduzirViolacaoDeIntegridade(usuario.getEmail());
        }
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario usuarioAtualizado) {
        Usuario existente = buscarPorId(id);

        validarEmailUnicoParaAtualizacao(usuarioAtualizado.getEmail(), id);
        aplicarAlteracoes(usuarioAtualizado, existente);

        try {
            Usuario usuarioSalvo = usuarioRepository.save(existente);
            usuarioRepository.flush();
            return usuarioSalvo;
        } catch (DataIntegrityViolationException ex) {
            throw traduzirViolacaoDeIntegridade(existente.getEmail());
        }
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }

    private void aplicarAlteracoes(Usuario origem, Usuario destino) {
        destino.setNome(origem.getNome());
        destino.setEmail(origem.getEmail());
        if (StringUtils.hasText(origem.getSenha())) {
            destino.setSenha(passwordEncoder.encode(origem.getSenha()));
        }
        destino.setPerfil(origem.getPerfil());
        destino.setAtivo(origem.getAtivo());
    }

    private void validarEmailUnico(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw traduzirViolacaoDeIntegridade(email);
        }
    }

    private void validarEmailUnicoParaAtualizacao(String email, Long idAtual) {
        if (usuarioRepository.existsByEmailAndIdNot(email, idAtual)) {
            throw traduzirViolacaoDeIntegridade(email);
        }
    }

    private RegraDeNegocioException traduzirViolacaoDeIntegridade(String email) {
        return new RegraDeNegocioException("Ja existe um usuario cadastrado com o e-mail: " + email);
    }
}
