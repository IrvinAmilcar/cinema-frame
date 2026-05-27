package br.com.cinema.frame.pedido;

import br.com.cinema.frame.domain.portal.promocao.Cupom;

import java.time.LocalDate;
import java.util.UUID;

public record CupomResponse(UUID id, String codigo, String tipo, boolean cumulativo, LocalDate validade) {
    public static CupomResponse from(Cupom c) {
        return new CupomResponse(c.getId(), c.getCodigo(), c.getTipo().name(), c.isCumulativo(), c.getValidade());
    }
}
