package br.com.cinema.frame.domain.backoffice.bomboniere;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class BombonieresSteps {

    private ProdutoDaBombonieresRepository produtoRepository = mock(ProdutoDaBombonieresRepository.class);
    private InsumoRepository insumoRepository = mock(InsumoRepository.class);
    private MovimentacaoEstoqueRepository movimentacaoRepository = mock(MovimentacaoEstoqueRepository.class);
    private BombonieresService gestao = new BombonieresService(produtoRepository, insumoRepository, movimentacaoRepository);

    private final Map<String, Insumo> insumos = new HashMap<>();
    private ProdutoDaBomboniere produto;
    private List<EstoqueNotificacao> notificacoes;
    private Exception excecaoCapturada;

    // ── DADO ──────────────────────────────────────────────

    @Dado("^(?:que )?existe o insumo cadastrado \"([^\"]+)\" com (\\d+) unidades em estoque e nível crítico de (\\d+) unidades$")
    public void insumoEmUnidades(String nome, int estoque, int critico) {
        Insumo insumo = new Insumo(nome, "unidade(s)", estoque, critico);
        insumos.put(nome, insumo);
        when(insumoRepository.buscarPorId(insumo.getId())).thenReturn(Optional.of(insumo));
    }

    @Dado("^(?:que )?existe o insumo cadastrado \"([^\"]+)\" com (\\d+)g em estoque e nível crítico de (\\d+)g$")
    public void insumoEmGramas(String nome, int estoque, int critico) {
        Insumo insumo = new Insumo(nome, "g", estoque, critico);
        insumos.put(nome, insumo);
        when(insumoRepository.buscarPorId(insumo.getId())).thenReturn(Optional.of(insumo));
    }

    @Dado("^(?:que )?existe o produto ativo cadastrado \"([^\"]+)\" com receita de (\\d+(?:\\.\\d+)?)g de \"([^\"]+)\" e (\\d+(?:\\.\\d+)?) \"([^\"]+)\"$")
    public void produtoAtivoCadastrado(String nomeProduto, double qtdGramas,
                                       String nomeInsumo1, double qtdUnidades,
                                       String nomeInsumo2) {
        produto = new ProdutoDaBomboniere(nomeProduto, 20.0, CategoriaProduto.COMBO);
        produto.adicionarItemReceita(insumos.get(nomeInsumo1), qtdGramas);
        produto.adicionarItemReceita(insumos.get(nomeInsumo2), qtdUnidades);
        when(produtoRepository.buscarPorId(produto.getId())).thenReturn(Optional.of(produto));
    }

    @Dado("^(?:que )?existe o produto inativo cadastrado \"([^\"]+)\" com receita de (\\d+(?:\\.\\d+)?)g de \"([^\"]+)\" e (\\d+(?:\\.\\d+)?) \"([^\"]+)\"$")
    public void produtoInativoCadastrado(String nomeProduto, double qtdGramas,
                                         String nomeInsumo1, double qtdUnidades,
                                         String nomeInsumo2) {
        produto = new ProdutoDaBomboniere(nomeProduto, 20.0, CategoriaProduto.COMBO);
        produto.desativar();
        produto.adicionarItemReceita(insumos.get(nomeInsumo1), qtdGramas);
        produto.adicionarItemReceita(insumos.get(nomeInsumo2), qtdUnidades);
        when(produtoRepository.buscarPorId(produto.getId())).thenReturn(Optional.of(produto));
    }

    // ── QUANDO ────────────────────────────────────────────

    @Quando("o sistema cadastrar o insumo {string} com unidade {string} quantidade {double} e nível crítico {double}")
    public void cadastrarInsumo(String nome, String unidade, double quantidade, double nivelCritico) {
        Insumo insumo = gestao.cadastrarInsumo(nome, unidade, quantidade, nivelCritico);
        insumos.put(nome, insumo);
        when(insumoRepository.buscarPorId(insumo.getId())).thenReturn(Optional.of(insumo));
    }

    @Quando("o sistema tentar cadastrar um insumo com nome vazio")
    public void tentarCadastrarInsumoNomeVazio() {
        try {
            gestao.cadastrarInsumo("", "g", 100, 10);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o sistema tentar cadastrar um insumo com quantidade negativa")
    public void tentarCadastrarInsumoQuantidadeNegativa() {
        try {
            gestao.cadastrarInsumo("Milho", "g", -1, 10);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o sistema cadastrar o produto {string} com preço {double} e categoria {string}")
    public void cadastrarProduto(String nome, double preco, String categoria) {
        produto = gestao.cadastrarProduto(nome, preco, CategoriaProduto.valueOf(categoria));
        when(produtoRepository.buscarPorId(produto.getId())).thenReturn(Optional.of(produto));
    }

    @Quando("o sistema tentar cadastrar um produto com nome vazio")
    public void tentarCadastrarNomeVazio() {
        try {
            gestao.cadastrarProduto("", 10.0, CategoriaProduto.BEBIDA);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o sistema registrar a venda do produto {string}")
    public void registrarVenda(String nomeProduto) {
        notificacoes = gestao.vender(produto.getId());
    }

    @Quando("o sistema tentar registrar a venda do produto {string}")
    public void tentarRegistrarVenda(String nomeProduto) {
        try {
            notificacoes = gestao.vender(produto.getId());
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o sistema estornar a venda do produto {string}")
    public void estornarVenda(String nomeProduto) {
        gestao.estornarVenda(produto.getId());
    }

    // ── ENTÃO ─────────────────────────────────────────────

    @Então("o insumo deve ser salvo no repositório")
    public void insumoSalvoNoRepositorio() {
        verify(insumoRepository, atLeastOnce()).salvar(any(Insumo.class));
    }

    @Então("o sistema deve rejeitar informando nome do insumo inválido")
    public void rejeitarNomeInsumoInvalido() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Nome"));
    }

    @Então("o sistema deve rejeitar informando quantidade inválida")
    public void rejeitarQuantidadeInvalida() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("negativa"));
    }

    @Então("o produto deve ser salvo no repositório")
    public void produtoSalvoNoRepositorio() {
        verify(produtoRepository).salvar(any(ProdutoDaBomboniere.class));
    }

    @Então("o sistema deve rejeitar informando nome inválido")
    public void rejeitarNomeInvalido() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Nome"));
    }

    @Então("^o estoque de \"([^\"]+)\" deve ser (\\d+(?:\\.\\d+)?)g$")
    public void verificarEstoqueGramas(String nome, double esperado) {
        assertEquals(esperado, insumos.get(nome).getQuantidadeEmEstoque(), 0.01);
    }

    @Então("^o estoque de \"([^\"]+)\" deve ser (\\d+(?:\\.\\d+)?) unidades$")
    public void verificarEstoqueUnidades(String nome, double esperado) {
        assertEquals(esperado, insumos.get(nome).getQuantidadeEmEstoque(), 0.01);
    }

    @Então("deve ter sido registrada uma movimentação de saída para {string}")
    public void verificarMovimentacaoSaida(String nomeInsumo) {
        verify(movimentacaoRepository, atLeastOnce()).salvar(argThat(m ->
            m.getTipo() == TipoMovimentacao.SAIDA &&
            m.getInsumoId().equals(insumos.get(nomeInsumo).getId())
        ));
    }

    @Então("deve ter sido registrada uma movimentação de entrada para {string}")
    public void verificarMovimentacaoEntrada(String nomeInsumo) {
        verify(movimentacaoRepository, atLeastOnce()).salvar(argThat(m ->
            m.getTipo() == TipoMovimentacao.ENTRADA &&
            m.getInsumoId().equals(insumos.get(nomeInsumo).getId())
        ));
    }

    @Então("o sistema deve rejeitar informando produto indisponível")
    public void rejeitarProdutoIndisponivel() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("indisponível"));
    }

    @Então("o sistema deve rejeitar informando estoque insuficiente")
    public void rejeitarEstoqueInsuficiente() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Estoque insuficiente"));
    }

    @Então("o sistema deve emitir notificação de estoque crítico para {string}")
    public void verificarNotificacao(String nomeInsumo) {
        assertNotNull(notificacoes);
        assertTrue(notificacoes.stream()
            .anyMatch(n -> n.getInsumo().getNome().equals(nomeInsumo)));
    }
}