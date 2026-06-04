package br.com.cinema.frame.domain.backoffice.caixa;

public record OcupacaoPorSessaoItem(
        String sessaoId,
        String filme,
        String horario,
        int salaNumero,
        String salaTipo,
        int capacidade,
        int ingressosVendidos,
        int diasVigencia
) {}