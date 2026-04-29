package br.com.cinema.frame.domain.portal.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.backoffice.bomboniere.CategoriaProduto;
import br.com.cinema.frame.domain.backoffice.bomboniere.Insumo;
import br.com.cinema.frame.domain.backoffice.bomboniere.InsumoRepository;
import br.com.cinema.frame.domain.backoffice.bomboniere.MovimentacaoEstoqueRepository;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBombonieresRepository;
import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoDeCompraService;
import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.TipoSala;
import br.com.cinema.frame.domain.portal.fidelidade.BeneficioRepository;
import br.com.cinema.frame.domain.portal.fidelidade.FidelidadeRepository;
import br.com.cinema.frame.domain.portal.fidelidade.FidelidadeService;
import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeCompras;
import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeComprasRepository;
import br.com.cinema.frame.domain.portal.reserva.ReservaDeAssento;
import br.com.cinema.frame.domain.portal.reserva.ReservaRepository;
import br.com.cinema.frame.domain.portal.reserva.ReservaService;
import br.com.cinema.frame.domain.portal.reserva.StatusReserva;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PedidoSteps {

    private final PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private final GradeDeExibicaoRepository gradeRepository = mock(GradeDeExibicaoRepository.class);
    private final ProdutoDaBombonieresRepository produtoRepository = mock(ProdutoDaBombonieresRepository.class);
    private final InsumoRepository insumoRepository = mock(InsumoRepository.class);
    private final MovimentacaoEstoqueRepository movimentacaoRepository = mock(MovimentacaoEstoqueRepository.class);
    private final IngressoRepository ingressoRepository = mock(IngressoRepository.class);
    private final FidelidadeRepository fidelidadeRepository = mock(FidelidadeRepository.class);
    private final BeneficioRepository beneficioRepository = mock(BeneficioRepository.class);
    private final HistoricoDeComprasRepository historicoRepository = mock(HistoricoDeComprasRepository.class);
    private final FilmeRepository filmeRepository = mock(FilmeRepository.class);
    private final ReservaRepository reservaRepository = mock(ReservaRepository.class);

    private final BombonieresService bombonieresService =
        new BombonieresService(produtoRepository, insumoRepository, movimentacaoRepository);
    private final FidelidadeService fidelidadeService =
        new FidelidadeService(fidelidadeRepository, beneficioRepository);
    private final ClassificacaoDeCompraService classificacaoService =
        new ClassificacaoDeCompraService(filmeRepository);
    private final ReservaService reservaService =
        new ReservaService(reservaRepository, gradeRepository);

    private final PedidoService pedidoService = new PedidoService(
        pedidoRepository, gradeRepository, bombonieresService,
        ingressoRepository, fidelidadeService, historicoRepository,
        classificacaoService, reservaService
    );

    private Sessao sessao;
    private GradeDeExibicao grade;
    private Pedido pedido;
    private ProdutoDaBomboniere produto;
    private Insumo insumoMilho;
    private ResultadoDoPedido resultado;
    private Exception excecaoCapturada;
    private UUID clienteId;
    private ReservaDeAssento reserva;

    private final List<Pedido> pedidosEmMemoria = new ArrayList<>();
    private final List<ReservaDeAssento> reservasEmMemoria = new ArrayList<>();

    @Dado("que existe uma sessão cadastrada para o pedido")
    public void existeSessaoCadastradaParaOPedido() {
        Filme filme = new Filme("Filme Teste", Duration.ofMinutes(120),
            ClassificacaoIndicativa.LIVRE, GeneroFilme.COMEDIA);
        when(filmeRepository.buscarPorId(filme.getId())).thenReturn(Optional.of(filme));
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, LocalDate.now().atTime(20, 0));

        grade = new GradeDeExibicao(LocalDate.now(), LocalDate.now().plusDays(7));
        grade.adicionarSessao(sessao);

        when(gradeRepository.listarTodas()).thenReturn(List.of(grade));

        configurarPedidoRepositorioEmMemoria();

        pedido = pedidoService.iniciar(sessao.getId());
    }

    @Dado("que existe uma sessão com filme classificado para maiores de {int} anos para o pedido")
    public void existeSessaoComFilmeClassificado(int idade) {
        ClassificacaoIndicativa classificacao = switch (idade) {
            case 10 -> ClassificacaoIndicativa.DEZ;
            case 12 -> ClassificacaoIndicativa.DOZE;
            case 14 -> ClassificacaoIndicativa.QUATORZE;
            case 16 -> ClassificacaoIndicativa.DEZESSEIS;
            case 18 -> ClassificacaoIndicativa.DEZOITO;
            default -> ClassificacaoIndicativa.LIVRE;
        };
        Filme filme = new Filme("Filme Restrito", Duration.ofMinutes(120),
            classificacao, GeneroFilme.ACAO);
        when(filmeRepository.buscarPorId(filme.getId())).thenReturn(Optional.of(filme));
        Sala sala = new Sala(1, 100, TipoSala.PADRAO);
        sessao = new Sessao(filme, sala, LocalDate.now().plusDays(1).atTime(20, 0));

        grade = new GradeDeExibicao(LocalDate.now(), LocalDate.now().plusDays(7));
        grade.adicionarSessao(sessao);

        when(gradeRepository.listarTodas()).thenReturn(List.of(grade));

        configurarPedidoRepositorioEmMemoria();

        pedido = pedidoService.iniciar(sessao.getId());
    }

    @Dado("existe o produto cadastrado {string} com {double}g de {string} em estoque e {int} {string} em estoque")
    public void existeProdutoCadastrado(String nomeProduto, double qtdMilho,
                                        String nomeInsumo1, int qtdEmbalagem,
                                        String nomeInsumo2) {
        insumoMilho = new Insumo(nomeInsumo1, "g", qtdMilho, 50);
        Insumo embalagem = new Insumo(nomeInsumo2, "unidade(s)", qtdEmbalagem, 0);

        produto = new ProdutoDaBomboniere(nomeProduto, 20.0, CategoriaProduto.COMBO);
        produto.adicionarItemReceita(insumoMilho, 200);
        produto.adicionarItemReceita(embalagem, 1);

        when(produtoRepository.buscarPorId(produto.getId())).thenReturn(Optional.of(produto));
        when(insumoRepository.buscarPorId(insumoMilho.getId())).thenReturn(Optional.of(insumoMilho));
        when(insumoRepository.buscarPorId(embalagem.getId())).thenReturn(Optional.of(embalagem));
    }

    @Dado("existe um cliente identificado com ID válido para o pedido")
    public void existeClienteIdentificado() {
        clienteId = UUID.randomUUID();
        when(historicoRepository.buscarPorClienteId(clienteId)).thenReturn(Optional.empty());
        when(fidelidadeRepository.buscarPorCliente(clienteId)).thenReturn(Optional.empty());
        pedido = pedidoService.iniciar(sessao.getId(), clienteId);
    }

    @Dado("o cliente tem uma reserva ativa vinculada ao pedido")
    public void clienteTemReservaAtiva() {
        LocalDateTime agora = LocalDateTime.now();
        reserva = new ReservaDeAssento(sessao, 10, agora);
        reservasEmMemoria.add(reserva);

        when(reservaRepository.buscarPorId(reserva.getId())).thenAnswer(inv ->
            reservasEmMemoria.stream()
                .filter(r -> r.getId().equals(inv.getArgument(0)))
                .findFirst());
        doAnswer(inv -> {
            ReservaDeAssento r = inv.getArgument(0);
            reservasEmMemoria.removeIf(x -> x.getId().equals(r.getId()));
            reservasEmMemoria.add(r);
            return null;
        }).when(reservaRepository).salvar(any());
        when(reservaRepository.buscarPorSessaoId(sessao.getId())).thenAnswer(inv ->
            new ArrayList<>(reservasEmMemoria));

        pedidoService.vincularReserva(pedido.getId(), reserva.getId());
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

    @Quando("o cliente finalizar o pedido cadastrado com valor {double} e data atual")
    public void finalizarPedidoComValorEData(double valor) {
        resultado = pedidoService.finalizar(pedido.getId(),
            StatusPagamento.APROVADO, valor, LocalDateTime.now());
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

    @Quando("o cliente tentar finalizar o pedido cadastrado com pagamento recusado")
    public void tentarFinalizarComPagamentoRecusado() {
        try {
            pedidoService.finalizar(pedido.getId(), StatusPagamento.RECUSADO);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o cliente tentar adicionar ingresso meia sem elegibilidade ao pedido cadastrado")
    public void tentarAdicionarMeiaSemElegibilidade() {
        try {
            pedidoService.adicionarIngresso(pedido.getId(), TipoIngresso.MEIA, false);
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o cliente tentar adicionar ingresso com data de nascimento de {int} anos ao pedido")
    public void tentarAdicionarIngressoComDataNascimento(int anos) {
        try {
            LocalDate dataNascimento = LocalDate.now().minusYears(anos);
            pedidoService.adicionarIngresso(pedido.getId(), TipoIngresso.INTEIRA, true, dataNascimento);
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

    @Então("o ingresso deve ser salvo no repositório de ingressos")
    public void ingressoDeveSerSalvoNoRepositorioDeIngressos() {
        verify(ingressoRepository, atLeastOnce()).salvar(any(Ingresso.class));
    }

    @Então("o cliente deve ter acumulado pontos de fidelidade")
    public void clienteDeveTermAcumuladoPontos() {
        verify(fidelidadeRepository, atLeastOnce()).salvar(any());
    }

    @Então("o gênero do filme deve ser registrado no histórico do cliente")
    public void generoDeveSerRegistradoNoHistorico() {
        verify(historicoRepository, atLeastOnce()).salvar(any(HistoricoDeCompras.class));
    }

    @Então("a reserva vinculada deve ter status CONFIRMADO")
    public void reservaDeveEstarConfirmada() {
        assertNotNull(reserva);
        assertEquals(StatusReserva.CONFIRMADO, reserva.getStatus());
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

    @Então("o sistema deve rejeitar informando pagamento não aprovado")
    public void rejeitarPagamentoNaoAprovado() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Pagamento não aprovado"));
    }

    @Então("o sistema deve rejeitar informando elegibilidade não comprovada")
    public void rejeitarElegibilidadeNaoComprovada() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Elegibilidade não comprovada"));
    }

    @Então("o sistema deve rejeitar informando idade insuficiente para o filme")
    public void rejeitarIdadeInsuficienteParaFilme() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("não tem idade permitida"));
    }

    private void configurarPedidoRepositorioEmMemoria() {
        doAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            pedidosEmMemoria.removeIf(e -> e.getId().equals(p.getId()));
            pedidosEmMemoria.add(p);
            return null;
        }).when(pedidoRepository).salvar(any(Pedido.class));

        when(pedidoRepository.buscarPorId(any())).thenAnswer(inv ->
            pedidosEmMemoria.stream()
                .filter(p -> p.getId().equals(inv.getArgument(0)))
                .findFirst());
    }
}
