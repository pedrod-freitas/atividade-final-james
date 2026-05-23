package br.com.fatec.catalogo.controllers;

import br.com.fatec.catalogo.models.ProdutoModel;
import br.com.fatec.catalogo.services.CategoriaService;
import br.com.fatec.catalogo.services.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService service;

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public String listar(@RequestParam(value = "nome", required = false) String nome,
                         @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                         Model model) {

        if (nome != null && !nome.isBlank()) {
            model.addAttribute("produtos", service.listarPorNome(nome));
        } else if (categoriaId != null) {
            model.addAttribute("produtos", service.listarPorCategoria(categoriaId));
        } else {
            model.addAttribute("produtos", service.listarTodos());
        }

        model.addAttribute("categorias", categoriaService.listarTodas());
        return "lista-produtos";
    }

    @GetMapping("/novo")
    public String exibirFormulario(Model model) {
        model.addAttribute("produto", new ProdutoModel());
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "cadastro-produto";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("produto") ProdutoModel produto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // Validação de quantidade negativa na camada de serviço
        if (produto.getQuantidade() != null && produto.getQuantidade() < 0) {
            result.rejectValue("quantidade", "erro.quantidade", "A quantidade em estoque não pode ser negativa.");
        }

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listarTodas());
            return "cadastro-produto";
        }

        try {
            service.salvar(produto);
        } catch (RuntimeException e) {
            result.rejectValue("nome", "erro.nome", e.getMessage());
            model.addAttribute("categorias", categoriaService.listarTodas());
            return "cadastro-produto";
        }

        String horario = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Produto salvo com sucesso! Última modificação: " + horario);

        return "redirect:/produtos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable long id, Model model) {
        model.addAttribute("produto", service.buscarPorId(id));
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "cadastro-produto";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable long id, RedirectAttributes redirectAttributes) {
        service.excluir(id);
        String horario = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        redirectAttributes.addFlashAttribute("mensagemSucesso",
                "Produto excluído com sucesso! Operação realizada em: " + horario);
        return "redirect:/produtos";
    }

    // Painel de Auditoria - apenas ADMIN
    @GetMapping("/auditoria")
    public String auditoria(Model model) {
        model.addAttribute("produtos", service.listarOrdenadoPorDataAtualizacao());
        return "auditoria-produtos";
    }
}