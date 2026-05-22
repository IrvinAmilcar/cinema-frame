package br.com.cinema.frame.sala;

import br.com.cinema.frame.domain.backoffice.sala.Sala;

import java.util.UUID;

public record SalaResponse(
    UUID id,
    int numero,
    int capacidade,
    String tipo
) {
    public static SalaResponse from(Sala s) {
        return new SalaResponse(s.getId(), s.getNumero(), s.getCapacidade(), s.getTipo().name());
    }
}
