package br.com.cinema.frame.domain.backoffice.bomboniere;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BombonieresService {

    private final ProdutoDaBombonieresRepository produtoRepository;
    private final InsumoRepository insumoRepository;

    public BombonieresService(ProdutoDaBombonieresRepository produtoRepository,
                               InsumoRepository insumoRepository) {
        if (produtoRepository == null)
            throw new IllegalArgumentException("ProdutoRepository não pode ser nulo");
        if (insumoRepository == null)
            throw new IllegalArgumentException("InsumoRepository não pode ser nulo");

        this.produtoRepository = produtoRepository;
        this.insumoRepository = insumoRepository;
    }

    public List<EstoqueNotificacao> vender(UUID produtoId) {
        if (produtoId == null)
            throw new IllegalArgumentException("ID do produto não pode ser nulo");

        ProdutoDaBomboniere produto = produtoRepository.buscarPorId(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produtoId));

        for (ItemDeReceita item : produto.getReceita()) {
            item.getInsumo().baixar(item.getQuantidade());
            insumoRepository.salvar(item.getInsumo());
        }

        List<EstoqueNotificacao> notificacoes = new ArrayList<>();
        for (ItemDeReceita item : produto.getReceita()) {
            if (item.getInsumo().isEstoqueCritico()) {
                notificacoes.add(new EstoqueNotificacao(item.getInsumo()));
            }
        }

        return notificacoes;
    }

    public ProdutoDaBomboniere buscarProdutoPorId(UUID id) {
        return produtoRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
    }

    public List<ProdutoDaBomboniere> listarProdutos() {
        return produtoRepository.listarTodos();
    }

    public List<EstoqueNotificacao> verificarEstoqueCritico() {
        List<EstoqueNotificacao> notificacoes = new ArrayList<>();
        for (Insumo insumo : insumoRepository.listarEstoqueCritico()) {
            notificacoes.add(new EstoqueNotificacao(insumo));
        }
        return notificacoes;
    }
}
