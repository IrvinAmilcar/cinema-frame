package br.com.cinema.frame.caixa;

public record BombonierePorSessaoResponse(
        String sessaoId,
        String filme,
        String horario,
        int salaNumero,
        String salaTipo,
        String produto,
        int quantidade,
        double valorTotal
) {}