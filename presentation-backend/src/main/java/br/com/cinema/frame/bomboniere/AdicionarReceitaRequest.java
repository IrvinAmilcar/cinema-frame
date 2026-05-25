package br.com.cinema.frame.bomboniere;

import java.util.UUID;

public record AdicionarReceitaRequest(
        UUID insumoId,
        double quantidade
) {
}