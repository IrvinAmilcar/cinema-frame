package br.com.cinema.frame.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.CategoriaProduto;
import br.com.cinema.frame.domain.backoffice.bomboniere.EstoqueObserver;
import br.com.cinema.frame.domain.backoffice.bomboniere.Insumo;
import br.com.cinema.frame.domain.backoffice.bomboniere.InsumoRepository;
import br.com.cinema.frame.domain.backoffice.bomboniere.ItemDeReceita;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBombonieresRepository;
import org.springframework.web.bind.annotation.*;
import br.com.cinema.frame.bomboniere.ProdutoBomboniereRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bomboniere")
@CrossOrigin(origins = "*")
public class BomboniereController {

    private final InsumoRepository insumoRepository;
    private final ProdutoDaBombonieresRepository produtoRepository;
    private final EstoqueObserver estoqueObserver;


    public BomboniereController(
            InsumoRepository insumoRepository,
            ProdutoDaBombonieresRepository produtoRepository,
            EstoqueObserver estoqueObserver
    ) {
        this.insumoRepository = insumoRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueObserver = estoqueObserver;
    }

    // =====================================================
    // INSUMOS
    // =====================================================

    @PostMapping("/insumos")
    public InsumoResponse cadastrarInsumo(

        
            @RequestBody InsumoRequest request
    ) {

        System.out.println("JAVA RECEBEU: " + request.quantidade());

        System.out.println("Recebendo insumo: " + request.nome() + ", unidade: " + request.unidade());

        Insumo insumo = new Insumo(
                request.nome(),
                request.unidade() != null ? request.unidade() : "UN",
                request.quantidade(),
                request.nivelCritico()
        );

        insumoRepository.salvar(insumo);

        return new InsumoResponse(
                insumo.getId(),
                insumo.getNome(),
                insumo.getUnidade(),
                insumo.getQuantidadeEmEstoque(),
                insumo.getNivelCritico()
        );
    }

    @PostMapping("/insumos/{id}/repor")
    public void reporEstoque(
            @PathVariable java.util.UUID id, 
            @RequestBody ReposicaoRequest request
    ) {
        
        Insumo insumo = insumoRepository.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Insumo não encontrado"));
        
        insumo.repor(request.quantidade());
        
        insumoRepository.salvar(insumo);
    }

    @GetMapping("/insumos")
    public List<InsumoResponse> listarInsumos() {

        return insumoRepository
                .listarTodos()
                .stream()
                .map(insumo -> new InsumoResponse(
                        insumo.getId(),
                        insumo.getNome(),
                        insumo.getUnidade(),
                        insumo.getQuantidadeEmEstoque(),
                        insumo.getNivelCritico()
                ))
                .toList();
    }

    // =====================================================
    // ALERTAS
    // =====================================================

    @GetMapping("/alertas")
    public List<InsumoResponse> listarAlertas() {

        return insumoRepository
                .listarEstoqueCritico()
                .stream()
                .map(insumo -> new InsumoResponse(
                        insumo.getId(),
                        insumo.getNome(),
                        insumo.getUnidade(),
                        insumo.getQuantidadeEmEstoque(),
                        insumo.getNivelCritico()
                ))
                .toList();
    }

    // =====================================================
    // PRODUTOS
    // =====================================================

    @PostMapping("/produtos")
    public ProdutoBomboniereResponse cadastrarProduto(
            @RequestBody ProdutoBomboniereRequest request
    ) {

        ProdutoDaBomboniere produto =
        new ProdutoDaBomboniere(
                request.nome(),
                request.preco().doubleValue(),  // double para o domínio
                CategoriaProduto.valueOf(request.categoria())
        );

produtoRepository.salvar(produto);

return new ProdutoBomboniereResponse(
        produto.getId(),
        produto.getNome(),
        BigDecimal.valueOf(produto.getPreco()),  // BigDecimal para o response
        produto.getCategoria().name(),
        produto.isAtivo(),
        List.of()
);
    }

    @GetMapping("/produtos")
        public List<ProdutoBomboniereResponse> listarProdutos() {
        return produtoRepository.listarTodos().stream()
        .map(produto -> new ProdutoBomboniereResponse(
            produto.getId(),
            produto.getNome(),
            java.math.BigDecimal.valueOf(produto.getPreco()),
            produto.getCategoria().name(),
            produto.isAtivo(),
            produto.getReceita().stream()
                .map(item -> new ProdutoBomboniereResponse.ItemReceitaResponse(
                    item.getInsumo().getId().toString(),
                    item.getInsumo().getNome(),
                    item.getQuantidade()
                ))
                .toList()
        ))
        .toList();
}

@PutMapping("/produtos/{id}")
public ProdutoBomboniereResponse editarProduto(
        @PathVariable UUID id,
        @RequestBody ProdutoBomboniereRequest request
) {
    ProdutoDaBomboniere produto = produtoRepository.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

    ProdutoDaBomboniere atualizado = ProdutoDaBomboniere.reconstituir(
            produto.getId(),
            request.nome(),
            request.preco().doubleValue(),
            CategoriaProduto.valueOf(request.categoria()),
            new ArrayList<ItemDeReceita>(produto.getReceita()),
            produto.isAtivo()
    );

    produtoRepository.salvar(atualizado);

    return new ProdutoBomboniereResponse(
            atualizado.getId(),
            atualizado.getNome(),
            BigDecimal.valueOf(atualizado.getPreco()),
            atualizado.getCategoria().name(),
            atualizado.isAtivo(),
            atualizado.getReceita().stream()
                .map(item -> new ProdutoBomboniereResponse.ItemReceitaResponse(
                    item.getInsumo().getId().toString(),
                    item.getInsumo().getNome(),
                    item.getQuantidade()
                ))
                .toList()
    );
}

@DeleteMapping("/produtos/{produtoId}/receita/{insumoId}")
public void removerItemReceita(
        @PathVariable UUID produtoId,
        @PathVariable String insumoId
) {
    ProdutoDaBomboniere produto = produtoRepository.buscarPorId(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

    produto.removerItemReceita(insumoId);
    produtoRepository.salvar(produto);
}

    // =====================================================
    // VENDA
    // =====================================================

    @PostMapping("/produtos/{id}/vender")
public String venderProduto(
        @PathVariable UUID id
) {

    ProdutoDaBomboniere produto =
            produtoRepository
                    .buscarPorId(id)
                    .orElseThrow(() ->
                            new RuntimeException("Produto não encontrado"));

    if (produto.getReceita().isEmpty()) {
        throw new RuntimeException(
                "Produto não possui receita cadastrada"
        );
    }

    for (var item : produto.getReceita()) {

        Insumo insumo =
                insumoRepository
                        .buscarPorId(item.getInsumo().getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Insumo não encontrado"
                                ));
        insumo.adicionarObservador(estoqueObserver);

        insumo.baixar(item.getQuantidade());

        insumoRepository.salvar(insumo);
    }

    return "Venda realizada com sucesso";
}
    // =====================================================
    // ESTORNO
    // =====================================================

    @PostMapping("/produtos/{id}/estornar")
    public String estornarVenda(
            @PathVariable UUID id
    ) {

        ProdutoDaBomboniere produto =
                produtoRepository
                        .buscarPorId(id)
                        .orElseThrow(() ->
                                new RuntimeException("Produto não encontrado"));

        for (var item : produto.getReceita()) {

            Insumo insumo =
                    insumoRepository
                            .buscarPorId(item.getInsumo().getId())
                            .orElseThrow(() ->
                                    new RuntimeException("Insumo não encontrado"));

            insumo.repor(item.getQuantidade());

            insumoRepository.salvar(insumo);
        }

        return "Estorno realizado com sucesso";
    }

    @PostMapping("/produtos/{produtoId}/receita")
public String adicionarReceita(
        @PathVariable UUID produtoId,
        @RequestBody AdicionarReceitaRequest request
) {

    ProdutoDaBomboniere produto =
            produtoRepository
                    .buscarPorId(produtoId)
                    .orElseThrow(() ->
                            new RuntimeException("Produto não encontrado"));

    Insumo insumo =
            insumoRepository
                    .buscarPorId(request.insumoId())
                    .orElseThrow(() ->
                            new RuntimeException("Insumo não encontrado"));

    produto.adicionarItemReceita(
            insumo,
            request.quantidade()
    );

    produtoRepository.salvar(produto);

    return "Receita adicionada com sucesso";
}
}