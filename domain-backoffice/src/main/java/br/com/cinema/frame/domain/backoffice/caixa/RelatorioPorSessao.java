package br.com.cinema.frame.domain.backoffice.caixa;

import java.util.List;

public class RelatorioPorSessao extends RelatorioFechamento {

    // Considera apenas sessões com pelo menos um ingresso vendido
    @Override
    protected double calcularTotalVendas(List<VendaDia> vendas) {
        return vendas.stream()
                .filter(v -> v.getIngressosVendidos() > 0)
                .mapToDouble(VendaDia::getValorArrecadado)
                .sum();
    }

    @Override
    protected int calcularTotalIngressos(List<VendaDia> vendas) {
        return vendas.stream()
                .filter(v -> v.getIngressosVendidos() > 0)
                .mapToInt(VendaDia::getIngressosVendidos)
                .sum();
    }

    @Override
    protected int calcularTotalSessoes(List<VendaDia> vendas) {
        return (int) vendas.stream()
                .filter(v -> v.getIngressosVendidos() > 0)
                .count();
    }

    // Média ponderada pela capacidade da sala (mais precisa que a média simples)
    @Override
    protected double calcularTaxaOcupacao(List<VendaDia> vendas) {
        int totalCapacidade = vendas.stream()
                .filter(v -> v.getIngressosVendidos() > 0)
                .mapToInt(VendaDia::getCapacidadeSala)
                .sum();
        if (totalCapacidade == 0) return 0.0;
        int totalIngressos = vendas.stream()
                .filter(v -> v.getIngressosVendidos() > 0)
                .mapToInt(VendaDia::getIngressosVendidos)
                .sum();
        return (double) totalIngressos / totalCapacidade * 100.0;
    }
}
