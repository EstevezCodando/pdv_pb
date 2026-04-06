package com.pdv.pontovenda.repository;

import com.pdv.pontovenda.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para operacoes de persistencia da entidade Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    long countByAtivoTrue();

    List<Usuario> findByAtivoTrue();

    Optional<Usuario> findByEmail(String email);
}
