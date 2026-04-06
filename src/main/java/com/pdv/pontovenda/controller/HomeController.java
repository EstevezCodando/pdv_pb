package com.pdv.pontovenda.controller;

import com.pdv.pontovenda.dto.ResumoIntegradoResponse;
import com.pdv.pontovenda.service.ResumoIntegradoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller da pagina inicial do sistema PDV.
 * Exibe o acesso aos modulos e um resumo integrado da aplicacao.
 */
@Controller
public class HomeController {

    private final ResumoIntegradoService resumoIntegradoService;

    public HomeController(ResumoIntegradoService resumoIntegradoService) {
        this.resumoIntegradoService = resumoIntegradoService;
    }

    @GetMapping("/")
    public String home(Model model) {
        ResumoIntegradoResponse resumo = resumoIntegradoService.gerarResumo();
        model.addAttribute("resumo", resumo);
        return "index";
    }
}
