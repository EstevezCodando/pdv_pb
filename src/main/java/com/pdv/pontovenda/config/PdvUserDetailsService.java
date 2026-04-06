package com.pdv.pontovenda.config;

import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.repository.UsuarioRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servico de autenticacao que carrega o usuario pelo e-mail.
 */
@Service
public class PdvUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public PdvUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + email));

        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new DisabledException("Usuario inativo: " + email);
        }

        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil()))
        );
    }
}
