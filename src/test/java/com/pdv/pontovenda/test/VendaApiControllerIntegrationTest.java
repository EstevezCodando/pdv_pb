package com.pdv.pontovenda.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.repository.ProdutoRepository;
import com.pdv.pontovenda.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("VendaApiControllerIntegrationTest")
class VendaApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve registrar venda integrada e refletir no resumo")
    void deveRegistrarVendaIntegrada() throws Exception {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "Operador Caixa", "caixa@pdv.com", "senha123", "OPERADOR", true));
        Produto produto = produtoRepository.save(new Produto(null, "Arroz", "Integral", new BigDecimal("10.00"), 10, "111", true));

        String payload = objectMapper.writeValueAsString(Map.of(
                "usuarioId", usuario.getId(),
                "formaPagamento", "PIX",
                "itens", java.util.List.of(Map.of("produtoId", produto.getId(), "quantidade", 2))
        ));

        mockMvc.perform(post("/api/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidadeTotalItens", is(2)))
                .andExpect(jsonPath("$.valorTotal", is(20.00)));

        mockMvc.perform(get("/api/integracao/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVendas", is(1)))
                .andExpect(jsonPath("$.faturamentoTotal", is(20.00)));
    }
}
