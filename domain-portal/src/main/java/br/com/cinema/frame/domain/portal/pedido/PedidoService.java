package br.com.cinema.frame.domain.portal.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.portal.pedido.StatusPagamento;

import java.util.UUID;

public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final GradeDeExibicaoRepository gradeRepository;
    private final BombonieresService bombonieresService;

    public PedidoService(PedidoRepository pedidoRepository,
                         GradeDeExibicaoRepository gradeRepository,
                         BombonieresService bombonieresService) {
        if (pedidoRepository == null)
            throw new IllegalArgumentException("PedidoRepository não pode ser nulo");
        if (gradeRepository == null)
            throw new IllegalArgumentException("GradeDeExibicaoRepository não pode ser nulo");
        if (bombonieresService == null)
            throw new IllegalArgumentException("BombonieresService não pode ser nulo");

        this.pedidoRepository = pedidoRepository;
        this.gradeRepository = gradeRepository;
        this.bombonieresService = bombonieresService;
    }

    public Pedido iniciar(UUID sessaoId) {
        if (sessaoId == null)
            throw new IllegalArgumentException("ID da sessão não pode ser nulo");

        Sessao sessao = buscarSessaoPorId(sessaoId);
        Pedido pedido = new Pedido(sessao);
        pedidoRepository.salvar(pedido);
        return pedido;
    }

    public void adicionarIngresso(UUID pedidoId, TipoIngresso tipo, boolean possuiElegibilidade) {
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo do ingresso não pode ser nulo");
        if (tipo == TipoIngresso.MEIA && !possuiElegibilidade)
            throw new IllegalStateException("Elegibilidade não comprovada para meia-entrada");

        Pedido pedido = buscarPedidoPorId(pedidoId);
        pedido.adicionarIngresso(tipo);
        pedidoRepository.salvar(pedido);
    }

    public void adicionarIngresso(UUID pedidoId, TipoIngresso tipo) {
        adicionarIngresso(pedidoId, tipo, true);
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
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");

        Pedido pedido = buscarPedidoPorId(pedidoId);
        ResultadoDoPedido resultado = pedido.finalizar(bombonieresService);
        pedidoRepository.salvar(pedido);
        return resultado;
    }

    public ResultadoDoPedido finalizar(UUID pedidoId, StatusPagamento statusPagamento) {
        if (pedidoId == null)
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        if (statusPagamento == null)
            throw new IllegalArgumentException("Status de pagamento não pode ser nulo");
        if (statusPagamento == StatusPagamento.RECUSADO)
            throw new IllegalStateException("Pagamento não aprovado");

        Pedido pedido = buscarPedidoPorId(pedidoId);
        ResultadoDoPedido resultado = pedido.finalizar(bombonieresService);
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