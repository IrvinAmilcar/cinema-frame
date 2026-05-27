package br.com.cinema.frame.pedido;

import br.com.cinema.frame.domain.portal.promocao.TipoPromocao;

import java.time.LocalDate;

public record CupomRequest(String codigo, TipoPromocao tipo, boolean cumulativo, LocalDate validade) {}
