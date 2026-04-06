package com.pdv.pontovenda.controller;

import com.pdv.pontovenda.config.PerfisUsuario;
import com.pdv.pontovenda.entity.Usuario;
import com.pdv.pontovenda.exception.RecursoNaoEncontradoException;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import com.pdv.pontovenda.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller MVC para gerenciamento de usuarios via interface web.
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final String REDIRECT_LISTAGEM = "redirect:/usuarios";
    private static final String VIEW_FORMULARIO = "usuario/formulario";
    private static final String VIEW_LISTAGEM = "usuario/listagem";

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        return VIEW_LISTAGEM;
    }

    @GetMapping("/novo")
    public String novoFormulario(Model model) {
        prepararFormulario(model, new Usuario(), "Cadastrar");
        return VIEW_FORMULARIO;
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("usuario") Usuario usuario,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            prepararFormulario(model, usuario, "Cadastrar");
            return VIEW_FORMULARIO;
        }

        try {
            usuarioService.salvar(usuario);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuario cadastrado com sucesso!");
            return REDIRECT_LISTAGEM;
        } catch (RegraDeNegocioException ex) {
            prepararFormulario(model, usuario, "Cadastrar");
            model.addAttribute("mensagemErro", ex.getMessage());
            return VIEW_FORMULARIO;
        }
    }

    @GetMapping("/editar/{id}")
    public String editarFormulario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            prepararFormulario(model, usuario, "Atualizar");
            return VIEW_FORMULARIO;
        } catch (RecursoNaoEncontradoException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return REDIRECT_LISTAGEM;
        }
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("usuario") Usuario usuario,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            prepararFormulario(model, usuario, "Atualizar");
            return VIEW_FORMULARIO;
        }

        try {
            usuarioService.atualizar(id, usuario);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuario atualizado com sucesso!");
            return REDIRECT_LISTAGEM;
        } catch (RegraDeNegocioException ex) {
            prepararFormulario(model, usuario, "Atualizar");
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
            usuarioService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuario excluido com sucesso!");
        } catch (RecursoNaoEncontradoException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return REDIRECT_LISTAGEM;
    }

    private void prepararFormulario(Model model, Usuario usuario, String acao) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", PerfisUsuario.DISPONIVEIS);
        model.addAttribute("acao", acao);
    }
}
