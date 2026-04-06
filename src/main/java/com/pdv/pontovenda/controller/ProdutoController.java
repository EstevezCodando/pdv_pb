package com.pdv.pontovenda.controller;

import com.pdv.pontovenda.entity.Produto;
import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import com.pdv.pontovenda.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller MVC para gerenciamento de produtos via interface web.
 */
@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    private static final String REDIRECT_LISTAGEM = "redirect:/produtos";
    private static final String VIEW_FORMULARIO = "produto/formulario";
    private static final String VIEW_LISTAGEM = "produto/listagem";

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public String listar(Model model) {
        List<Produto> produtos = produtoService.listarTodos();
        model.addAttribute("produtos", produtos);
        return VIEW_LISTAGEM;
    }

    @GetMapping("/novo")
    public String novoFormulario(Model model) {
        prepararFormulario(model, new Produto(), "Cadastrar");
        return VIEW_FORMULARIO;
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("produto") Produto produto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            prepararFormulario(model, produto, "Cadastrar");
            return VIEW_FORMULARIO;
        }

        try {
            produtoService.salvar(produto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto cadastrado com sucesso!");
            return REDIRECT_LISTAGEM;
        } catch (RegraDeNegocioException ex) {
            prepararFormulario(model, produto, "Cadastrar");
            model.addAttribute("mensagemErro", ex.getMessage());
            return VIEW_FORMULARIO;
        }
    }

    @GetMapping("/editar/{id}")
    public String editarFormulario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Produto produto = produtoService.buscarPorId(id);
            prepararFormulario(model, produto, "Atualizar");
            return VIEW_FORMULARIO;
        } catch (RecursoNaoEncontradoException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return REDIRECT_LISTAGEM;
        }
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("produto") Produto produto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            prepararFormulario(model, produto, "Atualizar");
            return VIEW_FORMULARIO;
        }

        try {
            produtoService.atualizar(id, produto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto atualizado com sucesso!");
            return REDIRECT_LISTAGEM;
        } catch (RegraDeNegocioException ex) {
            prepararFormulario(model, produto, "Atualizar");
            model.addAttribute("mensagemErro", ex.getMessage());
            return VIEW_FORMULARIO;
        } catch (RecursoNaoEncontradoException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return REDIRECT_LISTAGEM;
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto excluido com sucesso!");
        } catch (RecursoNaoEncontradoException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return REDIRECT_LISTAGEM;
    }

    private void prepararFormulario(Model model, Produto produto, String acao) {
        model.addAttribute("produto", produto);
        model.addAttribute("acao", acao);
    }
}

