package br.com.cinema.frame.pedido;

import java.util.UUID;

public record IniciarPedidoRequest(UUID sessaoId, UUID clienteId) {}
