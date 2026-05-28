package br.com.cinema.frame.application.compra;

import br.com.cinema.frame.domain.portal.pedido.Pedido;
import br.com.cinema.frame.domain.portal.pedido.PedidoService;

import java.util.UUID;

public class IniciarPedidoUseCase {

    private final PedidoService pedidoService;

    public IniciarPedidoUseCase(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public Pedido executar(UUID sessaoId, UUID clienteId) {
        return pedidoService.iniciar(sessaoId, clienteId);
    }
}
