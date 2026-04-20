package br.com.cinema.frame.domain.backoffice.bomboniere;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BombonieresSteps {

    private final Map<String, Insumo> insumos = new HashMap<>();
    private ProdutoDaBomboniere produto;
    private List<EstoqueNotificacao> notificacoes;
    private Exception excecaoCapturada;
    private final GestaoDeEstoque gestao = new GestaoDeEstoque();

    @Dado("que existe o insumo {string} com {double}g em estoque e nível crítico de {double}g")
    public void insumoEmGramas(String nome, double estoque, double critico) {
        insumos.put(nome, new Insumo(nome, "g", estoque, critico));
    }

    @Dado("existe o insumo {string} com {double} unidades em estoque e nível crítico de {double} unidades")
    public void insumoEmUnidades(String nome, double estoque, double critico) {
        insumos.put(nome, new Insumo(nome, "unidade(s)", estoque, critico));
    }

    @Dado("existe o produto {string} com receita de {double}g de {string} e {double} {string}")
    public void existeProdutoComReceita(String nomeProduto, double qtdGramas, String nomeInsumo1, double qtdUnidades, String nomeInsumo2) {
        produto = new ProdutoDaBomboniere(nomeProduto);
        produto.adicionarItemReceita(insumos.get(nomeInsumo1), qtdGramas);
        produto.adicionarItemReceita(insumos.get(nomeInsumo2), qtdUnidades);
    }

    @Quando("o sistema registrar a venda do produto {string}")
    public void registrarVenda(String nomeProduto) {
        notificacoes = gestao.vender(produto);
    }

    @Quando("o sistema tentar registrar a venda do produto {string}")
    public void tentarRegistrarVenda(String nomeProduto) {
        try {
            notificacoes = gestao.vender(produto);
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