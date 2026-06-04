package br.com.cinema.frame.caixa;

public record OcupacaoPorSessaoResponse(
        String sessaoId,
        String filme,
        String horario,
        int salaNumero,
        String salaTipo,
        int capacidade,
        int ingressosVendidos,
        int diasVigencia
) {}
