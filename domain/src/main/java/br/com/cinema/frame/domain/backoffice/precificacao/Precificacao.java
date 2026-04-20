package br.com.cinema.frame.domain.backoffice.precificacao;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import java.time.DayOfWeek;

public class Precificacao {

    private static final double PRECO_BASE = 20.0;

    public double calcularPreco(Sessao sessao) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");

        double preco = PRECO_BASE;

        preco = aplicarTipoSala(preco, sessao.getSala().getTipo());
        preco = aplicarDescontoSemanal(preco, sessao.getInicio().getDayOfWeek());

        return preco;
    }

    private double aplicarTipoSala(double preco, TipoSala tipo) {
        return preco + (preco * tipo.getPercentualAcrescimo());
    }

    private double aplicarDescontoSemanal(double preco, DayOfWeek dia) {
        DescontoSemanal desconto = DescontoSemanal.buscarPorDia(dia);
        return preco - (preco * desconto.getPercentualDesconto());
    }
}