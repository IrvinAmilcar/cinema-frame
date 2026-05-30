package br.com.cinema.frame.domain.portal.pedido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoDeCompraService;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.portal.fidelidade.FidelidadeService;
import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeCompras;
import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeComprasRepository;
import br.com.cinema.frame.domain.portal.reserva.ReservaService;

public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final GradeDeExibicaoRepository gradeRepository;
    private final BombonieresService bombonieresService;
    private final IngressoRepository ingressoRepository;
    private final FidelidadeService fidelidadeService;
    private final HistoricoDeComprasRepository historicoRepository;
    private final ClassificacaoDeCompraService classificacaoService;
    private final ReservaService reservaService;

    public PedidoService(PedidoRepository pedidoRepository,
                         GradeDeExibicaoRepository gradeRepository,
                         BombonieresService bombonieresService) {
        this(pedidoRepository, gradeRepository, bombonieresService, null, null, null, null, null);
    }

    public PedidoService(PedidoRepository pedidoRepository,
                         GradeDeExibicaoRepository gradeRepository,
                         BombonieresService bombonieresService,
                         IngressoRepository ingressoRepository,
                         FidelidadeService fidelidadeService,
                         HistoricoDeComprasRepository historicoRepository,
                         ClassificacaoDeCompraService classificacaoService,
                         ReservaService reservaService) {
        if (pedidoRepository == null) throw new IllegalArgumentException("PedidoRepository não pode ser nulo");
        if (gradeRepository == null) throw new IllegalArgumentException("GradeDeExibicaoRepository não pode ser nulo");
        if (bombonieresService == null) throw new IllegalArgumentException("BombonieresService não pode ser nulo");
        this.pedidoRepository = pedidoRepository;
        this.gradeRepository = gradeRepository;
        this.bombonieresService = bombonieresService;
        this.ingressoRepository = ingressoRepository;
        this.fidelidadeService = fidelidadeService;
        this.historicoRepository = historicoRepository;
        this.classificacaoService = classificacaoService;
        this.reservaService = reservaService;
    }

    public Pedido iniciar(UUID sessaoId) { return iniciar(sessaoId, null); }

    public Pedido iniciar(UUID sessaoId, UUID clienteId) {
        if (sessaoId == null) throw new IllegalArgumentException("ID da sessão não pode ser nulo");
        Sessao sessao = buscarSessaoPorId(sessaoId);
        Pedido pedido = new Pedido(sessao, clienteId);
        pedidoRepository.salvar(pedido);
        return pedido;
    }

    public void vincularReserva(UUID pedidoId, UUID reservaId) {
        if (pedidoId == null) throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (reservaId == null) throw new IllegalArgumentException("ID da reserva não pode ser nulo");
        Pedido pedido = buscarPedidoPorId(pedidoId);
        pedido.vincularReserva(reservaId);
        pedidoRepository.salvar(pedido);
    }

    public void adicionarIngresso(UUID pedidoId, TipoIngresso tipo) {
        adicionarIngresso(pedidoId, tipo, true, null);
    }

    public void adicionarIngresso(UUID pedidoId, TipoIngresso tipo, boolean possuiElegibilidade) {
        adicionarIngresso(pedidoId, tipo, possuiElegibilidade, null);
    }

    public void adicionarIngresso(UUID pedidoId, TipoIngresso tipo,
                                   boolean possuiElegibilidade, LocalDate dataNascimento) {
        if (pedidoId == null) throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        Pedido pedido = buscarPedidoPorId(pedidoId);
        if (classificacaoService != null && dataNascimento != null) {
            classificacaoService.validarCompra(dataNascimento, pedido.getSessao().getFilme().getId());
        }
        pedido.adicionarIngresso(tipo);
        pedidoRepository.salvar(pedido);
    }

    public void adicionarProduto(UUID pedidoId, UUID produtoId) {
        if (pedidoId == null) throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        Pedido pedido = buscarPedidoPorId(pedidoId);
        ProdutoDaBomboniere produto = bombonieresService.buscarProdutoPorId(produtoId);
        pedido.adicionarProduto(produto);
        pedidoRepository.salvar(pedido);
    }

    public ResultadoDoPedido finalizar(UUID pedidoId) {
        return finalizarInterno(pedidoId, null, null, false);
    }

    public ResultadoDoPedido finalizar(UUID pedidoId, StatusPagamento statusPagamento) {
        if (pedidoId == null) throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (statusPagamento == StatusPagamento.RECUSADO) throw new IllegalStateException("Pagamento não aprovado");
        return finalizarInterno(pedidoId, null, null, false);
    }

    public ResultadoDoPedido finalizar(UUID pedidoId, StatusPagamento statusPagamento,
                                       double valorTotal, LocalDateTime agora) {
        return finalizar(pedidoId, statusPagamento, valorTotal, agora, false);
    }

    public ResultadoDoPedido finalizar(UUID pedidoId, StatusPagamento statusPagamento,
                                       double valorTotal, LocalDateTime agora, boolean usarPontos) {
        if (pedidoId == null) throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (statusPagamento == StatusPagamento.RECUSADO) throw new IllegalStateException("Pagamento não aprovado");
        if (agora == null) throw new IllegalArgumentException("Data/hora não pode ser nula");
        return finalizarInterno(pedidoId, valorTotal, agora, usarPontos);
    }

    private ResultadoDoPedido finalizarInterno(UUID pedidoId, Double valorTotal,
                                                LocalDateTime agora, boolean usarPontos) {
        if (pedidoId == null) throw new IllegalArgumentException("ID do pedido não pode ser nulo");

        Pedido pedido = buscarPedidoPorId(pedidoId);
        ResultadoDoPedido resultado = pedido.finalizar(bombonieresService);

        if (ingressoRepository != null) {
            pedido.getIngressos().forEach(ingressoRepository::salvar);
        }

        if (historicoRepository != null && pedido.getClienteId() != null) {
            HistoricoDeCompras historico = historicoRepository
                .buscarPorClienteId(pedido.getClienteId())
                .orElse(new HistoricoDeCompras(pedido.getClienteId()));
            historico.registrarFilme(pedido.getSessao().getFilme().getId(),
                                     pedido.getSessao().getFilme().getGenero());
            historicoRepository.salvar(historico);
        }

        if (fidelidadeService != null && pedido.getClienteId() != null && agora != null) {
            LocalDate hoje = agora.toLocalDate();

            // 1. Debita os pontos ANTES de acumular (se o cliente escolheu usar)
            if (usarPontos) {
                try {
                    fidelidadeService.usarPontosNaCompra(pedido.getClienteId(), hoje);
                } catch (Exception ignored) {
                    // cliente sem conta de fidelidade ainda — ignora silenciosamente
                }
            }

            // 2. Acumula pontos do valor pago
            if (valorTotal != null && valorTotal > 0) {
                fidelidadeService.acumularPontos(pedido.getClienteId(), valorTotal, hoje, pedido.getSessao().getFilme().getTitulo());
            }
        }

        if (reservaService != null && pedido.getReservaId() != null && agora != null) {
            reservaService.confirmar(pedido.getReservaId(), agora);
        }

        pedidoRepository.salvar(pedido);
        return resultado;
    }

    private Pedido buscarPedidoPorId(UUID pedidoId) {
        return pedidoRepository.buscarPorId(pedidoId)
            .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + pedidoId));
    }

    private Sessao buscarSessaoPorId(UUID sessaoId) {
        return gradeRepository.listarTodas().stream()
            .flatMap(g -> g.getSessoes().stream())
            .filter(s -> s.getId().equals(sessaoId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + sessaoId));
    }
}