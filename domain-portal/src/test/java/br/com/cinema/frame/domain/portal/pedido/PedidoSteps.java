package br.com.cinema.frame.domain.portal.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.backoffice.bomboniere.Insumo;
import br.com.cinema.frame.domain.backoffice.bomboniere.InsumoRepository;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBombonieresRepository;
import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.Filme;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PedidoSteps {

    private PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private GradeDeExibicaoRepository gradeRepository = mock(GradeDeExibicaoRepository.class);
    private ProdutoDaBombonieresRepository produtoRepository = mock(ProdutoDaBombonieresRepository.class);
    private InsumoRepository insumoRepository = mock(InsumoRepository.class);
    private BombonieresService bombonieresService = new BombonieresService(produtoRepository, insumoRepository);
    private PedidoService pedidoService = new PedidoService(pedidoRepository, gradeRepository, bombonieresService);

    private Sessao sessao;
    private GradeDeExibicao grade;
    private Pedido pedido;
    private ProdutoDaBomboniere produto;
    private Insumo insumoMilho;
    private ResultadoDoPedido resultado;
    private Exception excecaoCapturada;

    // Lista mutável para simular o repositório de pedidos
    private final List<Pedido> pedidosEmMemoria = new ArrayList<>();

    @Dado("que existe uma sessão cadastrada para o pedido")
    public void existeSessaoCadastradaParaOPedido() {
        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120),
            ClassificacaoIndicativa.LIVRE, GeneroFilme.COMEDIA);
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, LocalDate.now().atTime(20, 0));

        grade = new GradeDeExibicao(LocalDate.now(), LocalDate.now().plusDays(7));
        grade.adicionarSessao(sessao);

        when(gradeRepository.listarTodas()).thenReturn(List.of(grade));

        // Configura repositório de pedidos para operar em memória
        doAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            pedidosEmMemoria.removeIf(existing -> existing.getId().equals(p.getId()));
            pedidosEmMemoria.add(p);
            return null;
        }).when(pedidoRepository).salvar(any(Pedido.class));
        when(pedidoRepository.buscarPorId(any())).thenAnswer(inv ->
            pedidosEmMemoria.stream().filter(p -> p.getId().equals(inv.getArgument(0))).findFirst());

        // Inicia o pedido já no step de sessão (será reutilizado pelos steps seguintes)
        pedido = pedidoService.iniciar(sessao.getId());
    }

    @Dado("existe o produto cadastrado {string} com {double}g de {string} em estoque e {int} {string} em estoque")
    public void existeProdutoCadastrado(String nomeProduto, double qtdMilho,
                                        String nomeInsumo1, int qtdEmbalagem,
                                        String nomeInsumo2) {
        insumoMilho = new Insumo(nomeInsumo1, "g", qtdMilho, 50);
        Insumo embalagem = new Insumo(nomeInsumo2, "unidade(s)", qtdEmbalagem, 0);

        produto = new ProdutoDaBomboniere(nomeProduto);
        produto.adicionarItemReceita(insumoMilho, 200);
        produto.adicionarItemReceita(embalagem, 1);

        when(produtoRepository.buscarPorId(produto.getId())).thenReturn(Optional.of(produto));
        when(insumoRepository.buscarPorId(insumoMilho.getId())).thenReturn(Optional.of(insumoMilho));
        when(insumoRepository.buscarPorId(embalagem.getId())).thenReturn(Optional.of(embalagem));
    }

    @Quando("o cliente adicionar {int} ingresso inteiro ao pedido cadastrado")
    public void adicionarIngressoInteiro(int quantidade) {
        for (int i = 0; i < quantidade; i++)
            pedidoService.adicionarIngresso(pedido.getId(), TipoIngresso.INTEIRA);
    }

    @Quando("o cliente adicionar o produto cadastrado {string} ao pedido cadastrado")
    public void adicionarProdutoCadastradoAoPedido(String nomeProduto) {
        pedidoService.adicionarProduto(pedido.getId(), produto.getId());
    }

    @Quando("o cliente finalizar o pedido cadastrado")
    public void finalizarPedidoCadastrado() {
        resultado = pedidoService.finalizar(pedido.getId());
    }

    @Quando("o cliente finalizar o pedido cadastrado sem produtos da bomboniere")
    public void finalizarPedidoCadastradoSemProdutos() {
        resultado = pedidoService.finalizar(pedido.getId());
    }

    @Quando("o cliente tentar finalizar o pedido cadastrado sem ingressos")
    public void tentarFinalizarSemIngressos() {
        try {
            pedidoService.finalizar(pedido.getId());
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o cliente tentar finalizar o pedido cadastrado com estoque insuficiente")
    public void tentarFinalizarComEstoqueInsuficiente() {
        try {
            pedidoService.finalizar(pedido.getId());
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Então("o resultado deve conter um QRCode")
    public void resultadoDeveConterQRCode() {
        assertNotNull(resultado);
        assertNotNull(resultado.getQrCode());
        assertTrue(resultado.getQrCode().getCodigo().startsWith("QR-"));
        verify(pedidoRepository, atLeastOnce()).salvar(any(Pedido.class));
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