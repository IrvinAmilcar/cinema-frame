package br.com.cinema.frame.domain.backoffice.bomboniere;

import br.com.cinema.frame.domain.backoffice.rbac.Permissao;
import br.com.cinema.frame.domain.backoffice.rbac.RbacService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BombonieresService implements EstoqueSubject {

    private final ProdutoDaBombonieresRepository produtoRepository;
    private final InsumoRepository insumoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final RbacService rbacService;
    private final List<EstoqueObserver> observadores = new ArrayList<>();

    // Construtor retrocompatível (sem RBAC)
    public BombonieresService(ProdutoDaBombonieresRepository produtoRepository,
                               InsumoRepository insumoRepository,
                               MovimentacaoEstoqueRepository movimentacaoRepository) {
        this(produtoRepository, insumoRepository, movimentacaoRepository, null);
    }

    public BombonieresService(ProdutoDaBombonieresRepository produtoRepository,
                               InsumoRepository insumoRepository,
                               MovimentacaoEstoqueRepository movimentacaoRepository,
                               RbacService rbacService) {
        if (produtoRepository == null)
            throw new IllegalArgumentException("ProdutoRepository não pode ser nulo");
        if (insumoRepository == null)
            throw new IllegalArgumentException("InsumoRepository não pode ser nulo");
        if (movimentacaoRepository == null)
            throw new IllegalArgumentException("MovimentacaoRepository não pode ser nulo");

        this.produtoRepository = produtoRepository;
        this.insumoRepository = insumoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.rbacService = rbacService;
    }

    @Override
    public void adicionarObservador(EstoqueObserver observer) {
        observadores.add(observer);
    }

    @Override
    public void removerObservador(EstoqueObserver observer) {
        observadores.remove(observer);
    }

    @Override
    public void notificarObservadores(Insumo insumo) {
        for (EstoqueObserver observer : observadores) {
            observer.notificarEstoqueCritico(insumo);
        }
    }

    public Insumo cadastrarInsumo(String nome, String unidade, double quantidade, double nivelCritico) {
        Insumo insumo = new Insumo(nome, unidade, quantidade, nivelCritico);
        insumoRepository.salvar(insumo);
        return insumo;
    }

    public ProdutoDaBomboniere cadastrarProduto(String nome, double preco, CategoriaProduto categoria) {
        ProdutoDaBomboniere produto = new ProdutoDaBomboniere(nome, preco, categoria);
        produtoRepository.salvar(produto);
        return produto;
    }

    // Venda sem verificação de RBAC (uso interno / portal do cliente)
    public List<EstoqueNotificacao> vender(UUID produtoId) {
        return venderInterno(produtoId);
    }

    // L10: Venda com verificação de RBAC (uso pelo operador de caixa no backoffice)
    public List<EstoqueNotificacao> vender(UUID funcionarioId, UUID produtoId) {
        if (funcionarioId == null)
            throw new IllegalArgumentException("ID do funcionário não pode ser nulo");
        if (rbacService == null)
            throw new IllegalStateException("RbacService não configurado");

        rbacService.verificar(funcionarioId, Permissao.VENDER_INGRESSO);
        return venderInterno(produtoId);
    }

    private List<EstoqueNotificacao> venderInterno(UUID produtoId) {
        if (produtoId == null)
            throw new IllegalArgumentException("ID do produto não pode ser nulo");

        ProdutoDaBomboniere produto = produtoRepository.buscarPorId(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produtoId));

        if (!produto.isAtivo())
            throw new IllegalStateException("Produto indisponível: " + produto.getNome());

        // RN 7: verificar estoque de TODOS os insumos antes de debitar qualquer um
        for (ItemDeReceita item : produto.getReceita()) {
            Insumo insumo = insumoRepository.buscarPorId(item.getInsumo().getId())
                .orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado: " + item.getInsumo().getNome()));
            if (insumo.getQuantidadeEmEstoque() < item.getQuantidade())
                throw new IllegalStateException(
                    "Estoque insuficiente para o insumo '" + insumo.getNome() + "': " +
                    "necessário " + item.getQuantidade() + ", disponível " + insumo.getQuantidadeEmEstoque());
        }

        for (ItemDeReceita item : produto.getReceita()) {
            item.getInsumo().baixar(item.getQuantidade());
            insumoRepository.salvar(item.getInsumo());

            movimentacaoRepository.salvar(new MovimentacaoEstoque(
                item.getInsumo().getId(),
                TipoMovimentacao.SAIDA,
                item.getQuantidade(),
                "Venda do produto: " + produto.getNome(),
                LocalDateTime.now()
            ));
        }

        List<EstoqueNotificacao> notificacoes = new ArrayList<>();
        for (ItemDeReceita item : produto.getReceita()) {
            if (item.getInsumo().isEstoqueCritico()) {
                notificarObservadores(item.getInsumo());
                notificacoes.add(new EstoqueNotificacao(item.getInsumo()));
            }
        }

        return notificacoes;
    }

    public void estornarVenda(UUID produtoId) {
        if (produtoId == null)
            throw new IllegalArgumentException("ID do produto não pode ser nulo");

        ProdutoDaBomboniere produto = produtoRepository.buscarPorId(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produtoId));

        for (ItemDeReceita item : produto.getReceita()) {
            item.getInsumo().repor(item.getQuantidade());
            insumoRepository.salvar(item.getInsumo());

            movimentacaoRepository.salvar(new MovimentacaoEstoque(
                item.getInsumo().getId(),
                TipoMovimentacao.ENTRADA,
                item.getQuantidade(),
                "Estorno da venda do produto: " + produto.getNome(),
                LocalDateTime.now()
            ));
        }
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
