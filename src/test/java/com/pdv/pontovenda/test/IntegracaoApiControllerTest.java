package com.pdv.pontovenda.test;

import com.pdv.pontovenda.controller.IntegracaoApiController;
import com.pdv.pontovenda.dto.ResumoIntegradoResponse;
import com.pdv.pontovenda.service.ResumoIntegradoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("IntegracaoApiController")
class IntegracaoApiControllerTest {

    @Test
    @DisplayName("Deve retornar resumo integrado com status 200")
    void deveRetornarResumoIntegrado() {
        ResumoIntegradoService resumoIntegradoService = mock(ResumoIntegradoService.class);
        ResumoIntegradoResponse resumoEsperado = new ResumoIntegradoResponse(
                3L,
                2L,
                5L,
                4L,
                120L,
                new BigDecimal("3450.90"),
                1L,
                8L,
                new BigDecimal("987.54"),
                new BigDecimal("123.44")
        );
        when(resumoIntegradoService.gerarResumo()).thenReturn(resumoEsperado);

        IntegracaoApiController controller = new IntegracaoApiController(resumoIntegradoService);

        var response = controller.resumo();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(resumoEsperado, response.getBody());
    }
}
