package br.com.cinema.frame.caixa;

public record ResumoPeriodoResponse(
        int totalIngressos,
        double totalIngressosValor,
        double totalBomboniere,
        double totalDescontoPontos,
        double receitaTotal
) {}
