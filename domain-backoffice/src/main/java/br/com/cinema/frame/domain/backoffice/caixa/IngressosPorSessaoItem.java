package br.com.cinema.frame.domain.backoffice.caixa;

public record IngressosPorSessaoItem(
        String sessaoId,
        String filme,
        String horario,
        int salaNumero,
        String salaTipo,
        int totalIngressos,
        int totalInteira,
        double valorTotal
) {}
