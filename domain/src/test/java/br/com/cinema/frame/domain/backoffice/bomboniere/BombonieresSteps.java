package br.com.cinema.frame.domain.backoffice.bomboniere;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class BombonieresSteps {

    private ProdutoDaBombonieresRepository produtoRepository = mock(ProdutoDaBombonieresRepository.class);
    private InsumoRepository insumoRepository = mock(InsumoRepository.class);
    private BombonieresService gestao = new BombonieresService(produtoRepository, insumoRepository);

    private final Map<String, Insumo> insumos = new HashMap<>();
    private ProdutoDaBomboniere produto;
    private List<EstoqueNotificacao> notificacoes;
    private Exception excecaoCapturada;

    @Dado("que existe o insumo cadastrado {string} com {double}g em estoque e nível crítico de {double}g")
    public void insumoEmGramasCadastrado(String nome, double estoque, double critico) {
        Insumo insumo = new Insumo(nome, "g", estoque, critico);
        insumos.put(nome, insumo);
        when(insumoRepository.buscarPorId(insumo.getId())).thenReturn(Optional.of(insumo));
    }

    @Dado("existe o insumo cadastrado {string} com {double} unidades em estoque e nível crítico de {double} unidades")
    public void insumoEmUnidadesCadastrado(String nome, double estoque, double critico) {
        Insumo insumo = new Insumo(nome, "unidade(s)", estoque, critico);
        insumos.put(nome, insumo);
        when(insumoRepository.buscarPorId(insumo.getId())).thenReturn(Optional.of(insumo));
    }

    @Dado("existe o produto cadastrado {string} com receita de {double}g de {string} e {double} {string}")
    public void existeProdutoCadastradoComReceita(String nomeProduto, double qtdGramas,
                                                   String nomeInsumo1, double qtdUnidades,
                                                   String nomeInsumo2) {
        produto = new ProdutoDaBomboniere(nomeProduto);
        produto.adicionarItemReceita(insumos.get(nomeInsumo1), qtdGramas);
        produto.adicionarItemReceita(insumos.get(nomeInsumo2), qtdUnidades);
        when(produtoRepository.buscarPorId(produto.getId())).thenReturn(Optional.of(produto));
    }

    @Quando("o sistema registrar a venda do produto cadastrado {string}")
    public void registrarVendaCadastrado(String nomeProduto) {
        notificacoes = gestao.vender(produto.getId());
    }

    @Quando("o sistema tentar registrar a venda do produto cadastrado {string}")
    public void tentarRegistrarVendaCadastrado(String nomeProduto) {
        try {
            notificacoes = gestao.vender(produto.getId());
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o estoque de {string} deve ser {double}g")
    public void verificarEstoqueGramas(String nome, double esperado) {
        assertEquals(esperado, insumos.get(nome).getQuantidadeEmEstoque(), 0.01);
    }

    @Então("o estoque de {string} deve ser {double} unidades")
    public void verificarEstoqueUnidades(String nome, double esperado) {
        assertEquals(esperado, insumos.get(nome).getQuantidadeEmEstoque(), 0.01);
    }

    @Então("o sistema deve emitir notificação de estoque crítico para {string}")
    public void verificarNotificacao(String nomeInsumo) {
        assertNotNull(notificacoes);
        boolean encontrou = notificacoes.stream()
            .anyMatch(n -> n.getInsumo().getNome().equals(nomeInsumo));
        assertTrue(encontrou, "Notificação esperada para: " + nomeInsumo);
    }

    @Então("o sistema deve rejeitar informando estoque insuficiente")
    public void rejeitarEstoqueInsuficiente() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Estoque insuficiente"));
    }
}