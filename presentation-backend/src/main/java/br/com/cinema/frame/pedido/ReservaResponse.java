package br.com.cinema.frame.pedido;

import br.com.cinema.frame.domain.portal.reserva.ReservaDeAssento;

import java.util.UUID;

public record ReservaResponse(UUID id, int numeroAssento, String status, String expiracaoEm) {
    public static ReservaResponse from(ReservaDeAssento r) {
        return new ReservaResponse(
                r.getId(),
                r.getNumeroAssento(),
                r.getStatus().name(),
                r.getExpiracaoEm().toString()
        );
    }
}
