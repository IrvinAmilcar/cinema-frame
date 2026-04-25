package br.com.cinema.frame.domain.portal.promocao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MotorDePromocoes {

    private static final double PERCENTUAL_PARCERIA_CARTAO = 0.15;
    private static final double PERCENTUAL_ESTUDANTE = 0.20;

    public AplicacaoDeDesconto aplicar(double valorTotal, int quantidadeIngressos,List<Cupom> cupons, LocalDate hoje) {
        if (valorTotal < 0)
            throw new IllegalArgumentException("Valor total não pode ser negativo");
        if (cupons == null)
            throw new IllegalArgumentException("Lista de cupons não pode ser nula");
        if (hoje == null)
            throw new IllegalArgumentException("Data atual não pode ser nula");

        validarCupons(cupons, hoje);

        double totalDesconto = 0.0;

        for (Cupom cupom : cupons) {
            double desconto = calcularDesconto(cupom, valorTotal, quantidadeIngressos);
            totalDesconto += desconto;
        }

        if (totalDesconto > valorTotal)
            totalDesconto = valorTotal;

        return new AplicacaoDeDesconto(valorTotal, totalDesconto);
    }

    private void validarCupons(List<Cupom> cupons, LocalDate hoje) {
        for (Cupom cupom : cupons) {
            if (!cupom.estaValido(hoje))
                throw new IllegalStateException("Cupom expirado: " + cupom.getCodigo());
        }

        boolean temNaoCumulativo = cupons.stream().anyMatch(c -> !c.isCumulativo());
        if (temNaoCumulativo && cupons.size() > 1)
            throw new IllegalStateException("Cupons não cumulativos não podem ser combinados");
    }

    private double calcularDesconto(Cupom cupom, double valorTotal, int quantidadeIngressos) {
        return switch (cupom.getTipo()) {
            case LEVE2_PAGUE1 -> calcularLeve2Pague1(valorTotal, quantidadeIngressos);
            case PARCERIA_CARTAO -> valorTotal * PERCENTUAL_PARCERIA_CARTAO;
            case DESCONTO_ESTUDANTE -> valorTotal * PERCENTUAL_ESTUDANTE;
        };
    }

    private double calcularLeve2Pague1(double valorTotal, int quantidadeIngressos) {
        if (quantidadeIngressos < 2)
            return 0.0;

        double precoUnitario = valorTotal / quantidadeIngressos;
        int pares = quantidadeIngressos / 2;
        return pares * precoUnitario;
    }
}