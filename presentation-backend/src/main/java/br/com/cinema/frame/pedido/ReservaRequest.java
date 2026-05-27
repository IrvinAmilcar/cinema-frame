package br.com.cinema.frame.pedido;

import java.util.UUID;

public record ReservaRequest(UUID sessaoId, int numeroAssento) {}
