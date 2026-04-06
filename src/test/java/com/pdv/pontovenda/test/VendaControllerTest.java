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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Testes de integracao para VendaController (fluxo MVC).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("VendaController")
class VendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /vendas - Deve exibir historico de vendas com lista vazia")
    void deveListarVendasVazias() throws Exception {
        mockMvc.perform(get("/vendas"))
                .andExpect(status().isOk())
                .andExpect(view().name("venda/listagem"))
                .andExpect(model().attributeExists("vendas"));
    }

    @Test
    @DisplayName("GET /vendas/nova - Deve exibir formulario de nova venda")
    void deveExibirFormularioNovaVenda() throws Exception {
        mockMvc.perform(get("/vendas/nova"))
                .andExpect(status().isOk())
                .andExpect(view().name("venda/formulario"))
                .andExpect(model().attributeExists("novaVendaForm"))
                .andExpect(model().attributeExists("formasPagamento"))
                .andExpect(model().attributeExists("usuarios"));
    }

    @Test
    @DisplayName("POST /vendas/nova - Deve rejeitar venda sem itens selecionados")
    void deveRejeitarVendaSemItens() throws Exception {
        mockMvc.perform(post("/vendas/nova")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("usuarioId", "1")
                        .param("formaPagamento", "PIX"))
                .andExpect(status().isOk())
                .andExpect(view().name("venda/formulario"))
                .andExpect(model().attributeExists("mensagemErro"));
    }

    @Test
    @DisplayName("POST /vendas/nova - Deve rejeitar venda sem operador informado")
    void deveRejeitarVendaSemOperador() throws Exception {
        mockMvc.perform(post("/vendas/nova")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("itens[0].produtoId", "1")
                        .param("itens[0].quantidade", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("venda/formulario"))
                .andExpect(model().attributeExists("mensagemErro"));
    }
}
