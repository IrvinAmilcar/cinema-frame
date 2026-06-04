package br.com.cinema.frame.domain.backoffice.caixa;

public record BombonierePorSessaoItem(
        String sessaoId,
        String filme,
        String horario,
        int salaNumero,
        String salaTipo,
        String produto,
        int quantidade,
        double valorTotal
) {}
