package br.com.cinema.frame.domain.backoffice.caixa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public abstract class RelatorioFechamento {

    // Template method — esqueleto fixo do algoritmo de fechamento
    public final FechamentoCaixa gerar(LocalDate data, List<VendaDia> vendas, LocalDateTime momento) {
        double totalVendas     = calcularTotalVendas(vendas);
        int    totalIngressos  = calcularTotalIngressos(vendas);
        int    totalSessoes    = calcularTotalSessoes(vendas);
        double taxaOcupacao    = calcularTaxaOcupacao(vendas);
        return new FechamentoCaixa(UUID.randomUUID(), data,
                totalVendas, totalIngressos, totalSessoes, taxaOcupacao, momento);
    }

    protected abstract double calcularTotalVendas(List<VendaDia> vendas);
    protected abstract int    calcularTotalIngressos(List<VendaDia> vendas);
    protected abstract int    calcularTotalSessoes(List<VendaDia> vendas);
    protected abstract double calcularTaxaOcupacao(List<VendaDia> vendas);
}
