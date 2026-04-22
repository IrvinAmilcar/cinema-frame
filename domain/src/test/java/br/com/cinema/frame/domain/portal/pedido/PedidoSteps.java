package br.com.cinema.frame.domain.portal.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.GestaoDeEstoque;
import br.com.cinema.frame.domain.backoffice.bomboniere.Insumo;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.precificacao.TipoSala;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.portal.pedido.Pedido;
import br.com.cinema.frame.domain.portal.pedido.ResultadoDoPedido;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PedidoSteps {

    private Sessao sessao;
    private Pedido pedido;
    private ProdutoDaBomboniere produto;
    private Insumo insumoMilho;
    private ResultadoDoPedido resultado;
    private Exception excecaoCapturada;
    private final GestaoDeEstoque gestaoDeEstoque = new GestaoDeEstoque();

    @Dado("que existe uma sessão para o pedido")
    public void existeSessaoParaOPedido() {
        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120), ClassificacaoIndicativa.LIVRE, GeneroFilme.COMEDIA);
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, LocalDate.now().atTime(20, 0));
        pedido = new Pedido(sessao);
    }

    @Dado("existe o produto {string} com {double}g de {string} em estoque e {int} {string} em estoque")
    public void existeProdutoComEstoque(String nomeProduto, double qtdMilho, String nomeInsumo1, int qtdEmbalagem, String nomeInsumo2) {
        insumoMilho = new Insumo(nomeInsumo1, "g", qtdMilho, 50);
        Insumo embalagem = new Insumo(nomeInsumo2, "unidade(s)", qtdEmbalagem, 0);

        produto = new ProdutoDaBomboniere(nomeProduto);
        produto.adicionarItemReceita(insumoMilho, 200);
        produto.adicionarItemReceita(embalagem, 1);
    }

    @Quando("o cliente adicionar {int} ingresso inteiro ao pedido")
    public void adicionarIngressoInteiro(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            pedido.adicionarIngresso(TipoIngresso.INTEIRA);
    }

    @Quando("o cliente adicionar o produto {string} ao pedido")
    public void adicionarProdutoAoPedido(String nomeProduto) {
        pedido.adicionarProduto(produto);
    }

    @Quando("o cliente finalizar o pedido")
    public void finalizarPedido() {
        resultado = pedido.finalizar(gestaoDeEstoque);
    }

    @Quando("o cliente finalizar o pedido sem produtos da bomboniere")
    public void finalizarPedidoSemProdutos() {
        resultado = pedido.finalizar(gestaoDeEstoque);
    }

    @Quando("o cliente tentar finalizar o pedido sem ingressos")
    public void tentarFinalizarSemIngressos() {
        try {
            pedido.finalizar(gestaoDeEstoque);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o cliente tentar finalizar o pedido com estoque insuficiente")
    public void tentarFinalizarComEstoqueInsuficiente() {
        try {
            pedido.finalizar(gestaoDeEstoque);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o resultado deve conter um QRCode")
    public void resultadoDeveConterQRCode() {
        assertNotNull(resultado);
        assertNotNull(resultado.getQrCode());
        assertTrue(resultado.getQrCode().getCodigo().startsWith("QR-"));
    }

    @Então("o resultado deve conter um Voucher")
    public void resultadoDeveConterVoucher() {
        assertTrue(resultado.temVoucher());
        assertTrue(resultado.getVoucher().get().getCodigo().startsWith("VCH-"));
    }

    @Então("o resultado não deve conter Voucher")
    public void resultadoNaoDeveConterVoucher() {
        assertFalse(resultado.temVoucher());
    }

    @Então("o estoque de {string} deve ter sido reduzido")
    public void estoqueDeveTermSidoReduzido(String nomeInsumo) {
        assertTrue(insumoMilho.getQuantidadeEmEstoque() < 1000);
    }

    @Então("o sistema deve rejeitar informando que o pedido precisa de ao menos um ingresso")
    public void rejeitarPedidoSemIngresso() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("ao menos um ingresso"));
    }

    @Então("o sistema deve rejeitar informando estoque insuficiente")
    public void rejeitarEstoqueInsuficiente() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Estoque insuficiente"));
    }
}