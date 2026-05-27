package br.com.cinema.frame.pedido;

import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;

public record AdicionarIngressoRequest(
        TipoIngresso tipo,
        Integer quantidade,
        boolean possuiElegibilidade,
        String dataNascimento   // ISO-8601: "YYYY-MM-DD" ou null
) {}
