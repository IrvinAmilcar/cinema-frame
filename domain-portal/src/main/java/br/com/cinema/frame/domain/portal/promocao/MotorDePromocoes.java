package br.com.cinema.frame.domain.portal.promocao;

import java.time.LocalDate;
import java.util.List;

public class MotorDePromocoes {

    private final List<DescontoStrategy> estrategias;

    public MotorDePromocoes(List<DescontoStrategy> estrategias) {
        if (estrategias == null)
            throw new IllegalArgumentException("Lista de estratégias não pode ser nula");
        this.estrategias = estrategias;
    }

    public AplicacaoDeDesconto aplicar(Cupom cupom, double valorTotal, int quantidadeIngressos, LocalDate hoje) {
        if (cupom == null)
            throw new IllegalArgumentException("Cupom não pode ser nulo");
        if (valorTotal < 0)
            throw new IllegalArgumentException("Valor total não pode ser negativo");
        if (hoje == null)
            throw new IllegalArgumentException("Data atual não pode ser nula");

        if (!cupom.estaValido(hoje))
            throw new IllegalStateException("Cupom expirado: " + cupom.getCodigo());

        // Padrão Strategy: seleciona a estratégia correta em tempo de execução pelo tipo do cupom
        DescontoStrategy estrategia = estrategias.stream()
            .filter(e -> e.getTipo() == cupom.getTipo())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Tipo de desconto não suportado: " + cupom.getTipo()));

        return estrategia.aplicar(valorTotal, quantidadeIngressos);
    }
}