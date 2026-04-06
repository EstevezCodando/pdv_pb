package com.pdv.pontovenda.test;

import com.pdv.pontovenda.config.PdvUserDetailsService;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios para PdvUserDetailsService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PdvUserDetailsService")
class PdvUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PdvUserDetailsService userDetailsService;

    @Test
    @DisplayName("Deve carregar usuario ativo pelo email com sucesso")
    void deveCarregarUsuarioAtivoPorEmail() {
        Usuario usuario = new Usuario(1L, "Admin", "admin@pdv.com",
                "$2a$10$hasheado", "ADMIN", true);
        when(usuarioRepository.findByEmail("admin@pdv.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@pdv.com");

        assertEquals("admin@pdv.com", userDetails.getUsername());
        assertEquals("$2a$10$hasheado", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Deve lancar UsernameNotFoundException para email inexistente")
    void deveLancarExcecaoParaEmailInexistente() {
        when(usuarioRepository.findByEmail("nao@existe.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nao@existe.com"));
    }

    @Test
    @DisplayName("Deve lancar DisabledException para usuario inativo")
    void deveLancarExcecaoParaUsuarioInativo() {
        Usuario inativo = new Usuario(2L, "Inativo", "inativo@pdv.com",
                "$2a$10$hasheado", "OPERADOR", false);
        when(usuarioRepository.findByEmail("inativo@pdv.com")).thenReturn(Optional.of(inativo));

        assertThrows(DisabledException.class,
                () -> userDetailsService.loadUserByUsername("inativo@pdv.com"));
    }
}
