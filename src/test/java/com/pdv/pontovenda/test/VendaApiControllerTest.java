package com.pdv.pontovenda.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integracao para VendaApiController (endpoints REST).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("VendaApiController")
class VendaApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/vendas - Deve retornar lista de vendas (vazia no inicio)")
    void deveListarVendasViaApi() throws Exception {
        mockMvc.perform(get("/api/vendas")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/vendas - Deve retornar 404 para usuario inexistente")
    void deveRetornarErroParaUsuarioInexistente() throws Exception {
        String payload = """
                {
                    "usuarioId": 99999,
                    "formaPagamento": "PIX",
                    "itens": [{"produtoId": 1, "quantidade": 1}]
                }
                """;

        mockMvc.perform(post("/api/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/vendas - Deve retornar 400 para corpo JSON invalido")
    void deveRetornarErroPorJsonInvalido() throws Exception {
        mockMvc.perform(post("/api/vendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalido}"))
                .andExpect(status().isBadRequest());
    }
}
