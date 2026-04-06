package com.pdv.pontovenda.controller;

import com.pdv.pontovenda.dto.CriarVendaRequest;
import com.pdv.pontovenda.dto.ItemVendaForm;
import com.pdv.pontovenda.dto.ItemVendaRequest;
import com.pdv.pontovenda.dto.NovaVendaForm;
import com.pdv.pontovenda.entity.FormaPagamento;
import com.pdv.pontovenda.exception.RegraDeNegocioException;
import com.pdv.pontovenda.service.ProdutoService;
import com.pdv.pontovenda.service.UsuarioService;
import com.pdv.pontovenda.service.VendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller MVC para o fluxo de vendas via interface web.
 */
@Controller
@RequestMapping("/vendas")
public class VendaController {

    private static final String REDIRECT_LISTAGEM = "redirect:/vendas";
    private static final String VIEW_FORMULARIO = "venda/formulario";
    private static final String VIEW_LISTAGEM = "venda/listagem";

    private final VendaService vendaService;
    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;

    public VendaController(VendaService vendaService,
                           ProdutoService produtoService,
                           UsuarioService usuarioService) {
        this.vendaService = vendaService;
        this.produtoService = produtoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vendas", vendaService.listarTodas());
        return VIEW_LISTAGEM;
    }

    @GetMapping("/nova")
    public String novaVenda(Model model) {
        NovaVendaForm form = new NovaVendaForm();
        produtoService.listarAtivos().stream()
                .map(p -> new ItemVendaForm(p.getId(), p.getNome(), p.getPreco()))
                .forEach(form.getItens()::add);

        prepararFormulario(model, form);
        return VIEW_FORMULARIO;
    }

    @PostMapping("/nova")
    public String registrar(@ModelAttribute("novaVendaForm") NovaVendaForm form,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        List<ItemVendaRequest> itens = form.getItens().stream()
                .filter(item -> item.getQuantidade() != null && item.getQuantidade() > 0)
                .map(item -> new ItemVendaRequest(item.getProdutoId(), item.getQuantidade()))
                .toList();

        if (itens.isEmpty()) {
            prepararFormulario(model, form);
            model.addAttribute("mensagemErro", "Selecione ao menos um produto com quantidade maior que zero.");
            return VIEW_FORMULARIO;
        }

        if (form.getUsuarioId() == null || form.getFormaPagamento() == null) {
            prepararFormulario(model, form);
            model.addAttribute("mensagemErro", "Operador e forma de pagamento sao obrigatorios.");
            return VIEW_FORMULARIO;
        }

        try {
            vendaService.registrarVenda(new CriarVendaRequest(form.getUsuarioId(), form.getFormaPagamento(), itens));
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Venda registrada com sucesso!");
            return REDIRECT_LISTAGEM;
        } catch (RegraDeNegocioException ex) {
            prepararFormulario(model, form);
            model.addAttribute("mensagemErro", ex.getMessage());
            return VIEW_FORMULARIO;
        }
    }

    private void prepararFormulario(Model model, NovaVendaForm form) {
        model.addAttribute("novaVendaForm", form);
        model.addAttribute("usuarios", usuarioService.listarAtivos());
        model.addAttribute("formasPagamento", FormaPagamento.values());
    }
}
