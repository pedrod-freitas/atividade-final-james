package br.com.fatec.catalogo.services;

import br.com.fatec.catalogo.models.ProdutoModel;
import br.com.fatec.catalogo.repositories.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository repository;

    public List<ProdutoModel> listarTodos() {
        return repository.findAll();
    }

    // Resolve o Desafio 1
    public List<ProdutoModel> listarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public ProdutoModel buscarPorId(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
    }

    public List<ProdutoModel> listarPorCategoria(Long idCategoria) {
        return repository.findByCategoriaIdCategoria(idCategoria);
    }

    // Painel de Auditoria: lista ordenado pela data de atualização mais recente
    public List<ProdutoModel> listarOrdenadoPorDataAtualizacao() {
        return repository.findAllByOrderByDataCadastroDesc();
    }

    // Resolve o Desafio 2 + Atividade Final (validação de quantidade)
    @Transactional
    public void salvar(ProdutoModel produto) {
        // Regra: Não permitir duplicidade de nome em novos registros
        if (produto.getIdProduto() == 0 && repository.existsByNome(produto.getNome())) {
            throw new RuntimeException("Já existe um produto com este nome.");
        }

        // Regra: Quantidade não pode ser negativa (validação na camada de serviço)
        if (produto.getQuantidade() != null && produto.getQuantidade() < 0) {
            throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa.");
        }

        // Atualiza a data a cada salvamento
        produto.setDataCadastro(LocalDateTime.now());

        repository.save(produto);
    }

    @Transactional
    public void excluir(long id) {
        repository.deleteById(id);
    }
}