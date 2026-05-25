package br.com.cinema.frame.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.CategoriaProduto;
import br.com.cinema.frame.domain.backoffice.bomboniere.Insumo;
import br.com.cinema.frame.domain.backoffice.bomboniere.InsumoRepository;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBombonieresRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bomboniere")
public class BomboniereController {

    private final InsumoRepository insumoRepository;
    private final ProdutoDaBombonieresRepository produtoRepository;

    public BomboniereController(
            InsumoRepository insumoRepository,
            ProdutoDaBombonieresRepository produtoRepository
    ) {
        this.insumoRepository = insumoRepository;
        this.produtoRepository = produtoRepository;
    }

    // =====================================================
    // INSUMOS
    // =====================================================

    @PostMapping("/insumos")
    public InsumoResponse cadastrarInsumo(
            @RequestBody InsumoRequest request
    ) {

        Insumo insumo = new Insumo(
                request.nome(),
                "UN",
                request.quantidade(),
                request.nivelCritico()
        );

        insumoRepository.salvar(insumo);

        return new InsumoResponse(
                insumo.getId(),
                insumo.getNome(),
                (int) insumo.getQuantidadeEmEstoque(),
                (int) insumo.getNivelCritico()
        );
    }

    @GetMapping("/insumos")
    public List<InsumoResponse> listarInsumos() {

        return insumoRepository
                .listarTodos()
                .stream()
                .map(insumo -> new InsumoResponse(
                        insumo.getId(),
                        insumo.getNome(),
                        (int) insumo.getQuantidadeEmEstoque(),
                        (int) insumo.getNivelCritico()
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
                        (int) insumo.getQuantidadeEmEstoque(),
                        (int) insumo.getNivelCritico()
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
                        request.preco().doubleValue(),
                        CategoriaProduto.COMBO
                );

        produtoRepository.salvar(produto);

        return new ProdutoBomboniereResponse(
                produto.getId(),
                produto.getNome(),
                request.preco()
        );
    }

    @GetMapping("/produtos")
    public List<ProdutoBomboniereResponse> listarProdutos() {

        return produtoRepository
                .listarTodos()
                .stream()
                .map(produto -> new ProdutoBomboniereResponse(
                        produto.getId(),
                        produto.getNome(),
                        java.math.BigDecimal.valueOf(produto.getPreco())
                ))
                .toList();
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

        for (var item : produto.getReceita()) {

            Insumo insumo =
                    insumoRepository
                            .buscarPorId(item.getInsumo().getId())
                            .orElseThrow(() ->
                                    new RuntimeException("Insumo não encontrado"));

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
}