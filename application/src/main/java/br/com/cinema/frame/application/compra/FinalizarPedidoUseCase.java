package br.com.cinema.frame.application.compra;

import br.com.cinema.frame.domain.portal.pedido.PedidoService;
import br.com.cinema.frame.domain.portal.pedido.ResultadoDoPedido;
import br.com.cinema.frame.domain.portal.pedido.StatusPagamento;

import java.time.LocalDateTime;
import java.util.UUID;

public class FinalizarPedidoUseCase {

    private final PedidoService pedidoService;

    public FinalizarPedidoUseCase(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public ResultadoDoPedido executar(UUID pedidoId) {
        return pedidoService.finalizar(pedidoId, StatusPagamento.APROVADO, 0.0, LocalDateTime.now());
    }
}
