package br.com.cinema.frame.domain.portal.pedido;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final GradeDeExibicaoRepository gradeRepository;
    private final BombonieresService bombonieresService;
    private final IngressoRepository ingressoRepository;
    private final FidelidadeService fidelidadeService;
    private final HistoricoDeComprasRepository historicoRepository;
    private final ClassificacaoDeCompraService classificacaoService;
    private final ReservaService reservaService;

    // Construtor retrocompatível (sem as novas dependências opcionais)
    public PedidoService(PedidoRepository pedidoRepository,
                         GradeDeExibicaoRepository gradeRepository,
                         BombonieresService bombonieresService) {
        this(pedidoRepository, gradeRepository, bombonieresService,
             null, null, null, null, null);
    }

    public PedidoService(PedidoRepository pedidoRepository,
                         GradeDeExibicaoRepository gradeRepository,
                         BombonieresService bombonieresService,
                         IngressoRepository ingressoRepository,
                         FidelidadeService fidelidadeService,
                         HistoricoDeComprasRepository historicoRepository,
                         ClassificacaoDeCompraService classificacaoService,
                         ReservaService reservaService) {
        if (pedidoRepository == null)
            throw new IllegalArgumentException("PedidoRepository não pode ser nulo");
        if (gradeRepository == null)
            throw new IllegalArgumentException("GradeDeExibicaoRepository não pode ser nulo");
        if (bombonieresService == null)
            throw new IllegalArgumentException("BombonieresService não pode ser nulo");

        this.pedidoRepository = pedidoRepository;
        this.gradeRepository = gradeRepository;
        this.bombonieresService = bombonieresService;
        this.ingressoRepository = ingressoRepository;
        this.fidelidadeService = fidelidadeService;
        this.historicoRepository = historicoRepository;
        this.classificacaoService = classificacaoService;
        this.reservaService = reservaService;
    }

    public Pedido iniciar(UUID sessaoId) {
        return iniciar(sessaoId, null);
    }

    public Pedido iniciar(UUID sessaoId, UUID clienteId) {
        if (sessaoId == null)
            throw new IllegalArgumentException("ID da sessão não pode ser nulo");

        Sessao sessao = buscarSessaoPorId(sessaoId);
        Pedido pedido = new Pedido(sessao, clienteId);
        pedidoRepository.salvar(pedido);
        return pedido;
    }

    public void vincularReserva(UUID pedidoId, UUID reservaId) {
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (reservaId == null)
            throw new IllegalArgumentException("ID da reserva não pode ser nulo");

        Pedido pedido = buscarPedidoPorId(pedidoId);
        pedido.vincularReserva(reservaId);
        pedidoRepository.salvar(pedido);
    }

    public void adicionarIngresso(UUID pedidoId, TipoIngresso tipo, boolean possuiElegibilidade) {
        adicionarIngresso(pedidoId, tipo, possuiElegibilidade, null);
    }

    public void adicionarIngresso(UUID pedidoId, TipoIngresso tipo) {
        adicionarIngresso(pedidoId, tipo, true, null);
    }

    // L5: overload com dataNascimento para validar classificação indicativa
    public void adicionarIngresso(UUID pedidoId, TipoIngresso tipo,
                                  boolean possuiElegibilidade, LocalDate dataNascimento) {
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo do ingresso não pode ser nulo");
        if (tipo == TipoIngresso.MEIA && !possuiElegibilidade)
            throw new IllegalStateException("Elegibilidade não comprovada para meia-entrada");

        Pedido pedido = buscarPedidoPorId(pedidoId);

        // L5: valida classificação indicativa se data de nascimento fornecida
        if (classificacaoService != null && dataNascimento != null) {
            classificacaoService.validarCompra(dataNascimento,
                pedido.getSessao().getFilme().getId());
        }

        pedido.adicionarIngresso(tipo);
        pedidoRepository.salvar(pedido);
    }

    public void adicionarProduto(UUID pedidoId, UUID produtoId) {
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (produtoId == null)
            throw new IllegalArgumentException("ID do produto não pode ser nulo");

        Pedido pedido = buscarPedidoPorId(pedidoId);
        ProdutoDaBomboniere produto = bombonieresService.buscarProdutoPorId(produtoId);
        pedido.adicionarProduto(produto);
        pedidoRepository.salvar(pedido);
    }

    public ResultadoDoPedido finalizar(UUID pedidoId) {
        return finalizarInterno(pedidoId, null, null);
    }

    public ResultadoDoPedido finalizar(UUID pedidoId, StatusPagamento statusPagamento) {
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (statusPagamento == null)
            throw new IllegalArgumentException("Status de pagamento não pode ser nulo");
        if (statusPagamento == StatusPagamento.RECUSADO)
            throw new IllegalStateException("Pagamento não aprovado");

        return finalizarInterno(pedidoId, null, null);
    }

    // Overload completo: valida pagamento + acumula fidelidade + registra histórico + confirma reserva
    public ResultadoDoPedido finalizar(UUID pedidoId, StatusPagamento statusPagamento,
                                       double valorTotal, LocalDateTime agora) {
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (statusPagamento == null)
            throw new IllegalArgumentException("Status de pagamento não pode ser nulo");
        if (statusPagamento == StatusPagamento.RECUSADO)
            throw new IllegalStateException("Pagamento não aprovado");
        if (agora == null)
            throw new IllegalArgumentException("Data/hora não pode ser nula");

        return finalizarInterno(pedidoId, valorTotal, agora);
    }

    private ResultadoDoPedido finalizarInterno(UUID pedidoId, Double valorTotal, LocalDateTime agora) {
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");

        Pedido pedido = buscarPedidoPorId(pedidoId);
        ResultadoDoPedido resultado = pedido.finalizar(bombonieresService);

        // L1: persiste cada ingresso no IngressoRepository para que o check-in funcione
        if (ingressoRepository != null) {
            pedido.getIngressos().forEach(ingressoRepository::salvar);
        }

        // L4: registra o gênero do filme no histórico do cliente para recomendações
        if (historicoRepository != null && pedido.getClienteId() != null) {
            HistoricoDeCompras historico = historicoRepository
                .buscarPorClienteId(pedido.getClienteId())
                .orElse(new HistoricoDeCompras(pedido.getClienteId()));
            historico.registrarGenero(pedido.getSessao().getFilme().getGenero());
            historicoRepository.salvar(historico);
        }

        // L3: acumula pontos de fidelidade com base no valor pago
        if (fidelidadeService != null && pedido.getClienteId() != null
                && valorTotal != null && agora != null) {
            fidelidadeService.acumularPontos(pedido.getClienteId(), valorTotal, agora.toLocalDate());
        }

        // L6: confirma a reserva de assento vinculada ao pedido
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
