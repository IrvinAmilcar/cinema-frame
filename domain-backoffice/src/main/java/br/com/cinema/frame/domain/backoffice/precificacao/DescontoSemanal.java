package br.com.cinema.frame.domain.backoffice.precificacao;

import java.time.DayOfWeek;

public enum DescontoSemanal {

    SEGUNDA(DayOfWeek.MONDAY, 0.30),
    TERCA(DayOfWeek.TUESDAY, 0.50),
    QUARTA(DayOfWeek.WEDNESDAY, 0.30),
    QUINTA(DayOfWeek.THURSDAY, 0.0),
    SEXTA(DayOfWeek.FRIDAY, 0.0),
    SABADO(DayOfWeek.SATURDAY, 0.0),
    DOMINGO(DayOfWeek.SUNDAY, 0.0);

    private final DayOfWeek diaDaSemana;
    private final double percentualDesconto;

    DescontoSemanal(DayOfWeek diaDaSemana, double percentualDesconto) {
        this.diaDaSemana = diaDaSemana;
        this.percentualDesconto = percentualDesconto;
    }

    public static DescontoSemanal buscarPorDia(DayOfWeek dia) {
        for (DescontoSemanal desconto : values()) {
            if (desconto.diaDaSemana == dia)
                return desconto;
        }
        throw new IllegalArgumentException("Dia da semana inválido: " + dia);
    }

    public double getPercentualDesconto() {
        return percentualDesconto;
    }

    public DayOfWeek getDiaDaSemana() {
        return diaDaSemana;
    }
}
