package br.com.cinema.frame.caixa;

public record IngressosPorSessaoResponse(
        String sessaoId,
        String filme,
        String horario,
        int salaNumero,
        String salaTipo,
        int totalIngressos,
        int totalInteira,
        double valorTotal
) {}
