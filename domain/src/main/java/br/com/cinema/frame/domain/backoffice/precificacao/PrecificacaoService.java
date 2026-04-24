package br.com.cinema.frame.domain.backoffice.precificacao;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

public class PrecificacaoService {

    private static final double PRECO_BASE = 20.0;

    public double calcularPreco(Sessao sessao) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");

        double preco = PRECO_BASE;
        preco = aplicarTipoSala(preco, sessao.getSala().getTipo());
        preco = aplicarDescontoSemanal(preco, sessao.getInicio().getDayOfWeek());
        return preco;
    }

    private double aplicarTipoSala(double preco, br.com.cinema.frame.domain.backoffice.sala.TipoSala tipo) {
        return preco + (preco * tipo.getPercentualAcrescimo());
    }

    private double aplicarDescontoSemanal(double preco, java.time.DayOfWeek dia) {
        DescontoSemanal desconto = DescontoSemanal.buscarPorDia(dia);
        return preco - (preco * desconto.getPercentualDesconto());
    }
}