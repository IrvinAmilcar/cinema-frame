package br.com.cinema.frame.domain.backoffice.caixa;

import java.util.List;

public class RelatorioDiario extends RelatorioFechamento {

    @Override
    protected double calcularTotalVendas(List<VendaDia> vendas) {
        return vendas.stream().mapToDouble(VendaDia::getValorArrecadado).sum();
    }

    @Override
    protected int calcularTotalIngressos(List<VendaDia> vendas) {
        return vendas.stream().mapToInt(VendaDia::getIngressosVendidos).sum();
    }

    @Override
    protected int calcularTotalSessoes(List<VendaDia> vendas) {
        return vendas.size();
    }

    @Override
    protected double calcularTaxaOcupacao(List<VendaDia> vendas) {
        return vendas.stream()
                .mapToDouble(VendaDia::getOcupacaoPercentual)
                .average()
                .orElse(0.0);
    }
}
