package com.pdv.pontovenda.config;

import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.repository.ProdutoRepository;
import com.pdv.pontovenda.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Inicializa usuarios e produtos minimos para ambientes locais.
 */
@Component
@Profile({"h2", "postgres"})
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            ProdutoRepository produtoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        criarUsuarioSeNaoExistir("Administrador", "admin@pdv.com", "admin123", PerfisUsuario.ADMIN, true);
        criarUsuarioSeNaoExistir("Operador PDV", "operador@pdv.com", "op123", PerfisUsuario.OPERADOR, true);
        criarUsuarioSeNaoExistir("Maria Silva", "maria@pdv.com", "maria123", PerfisUsuario.OPERADOR, true);

        if (produtoRepository.count() == 0) {
            produtoRepository.save(new Produto(null, "Cafe Tradicional", "Pacote de cafe 500g", new BigDecimal("18.90"), 25, "789100000001", true));
            produtoRepository.save(new Produto(null, "Leite Integral", "Caixa de leite 1L", new BigDecimal("5.99"), 40, "789100000002", true));
            produtoRepository.save(new Produto(null, "Pao de Forma", "Pacote de pao de forma 500g", new BigDecimal("8.49"), 18, "789100000003", true));
        }
    }

    private void criarUsuarioSeNaoExistir(String nome, String email, String senha, String perfil, boolean ativo) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return;
        }
        usuarioRepository.save(new Usuario(null, nome, email, passwordEncoder.encode(senha), perfil, ativo));
    }
}
