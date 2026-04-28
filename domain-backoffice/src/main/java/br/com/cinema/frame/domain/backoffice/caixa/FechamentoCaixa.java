package br.com.cinema.frame.domain.backoffice.caixa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class FechamentoCaixa {

    private final UUID id;
    private final LocalDate data;
    private final double totalVendas;
    private final int totalIngressos;
    private final int totalSessoes;
    private final double taxaOcupacaoMedia; // percentual
    private final LocalDateTime momentoFechamento;

    public FechamentoCaixa(UUID id, LocalDate data, double totalVendas,
                           int totalIngressos, int totalSessoes,
                           double taxaOcupacaoMedia, LocalDateTime momentoFechamento) {
        if (id == null) throw new IllegalArgumentException("Id é obrigatório");
        if (data == null) throw new IllegalArgumentException("Data é obrigatória");
        if (totalVendas < 0) throw new IllegalArgumentException("Total de vendas não pode ser negativo");
        if (totalIngressos < 0) throw new IllegalArgumentException("Total de ingressos não pode ser negativo");
        if (totalSessoes < 0) throw new IllegalArgumentException("Total de sessões não pode ser negativo");
        if (taxaOcupacaoMedia < 0 || taxaOcupacaoMedia > 100)
            throw new IllegalArgumentException("Taxa de ocupação deve estar entre 0 e 100");
        if (momentoFechamento == null) throw new IllegalArgumentException("Momento do fechamento é obrigatório");

        this.id = id;
        this.data = data;
        this.totalVendas = totalVendas;
        this.totalIngressos = totalIngressos;
        this.totalSessoes = totalSessoes;
        this.taxaOcupacaoMedia = taxaOcupacaoMedia;
        this.momentoFechamento = momentoFechamento;
    }

    public UUID getId() { return id; }
    public LocalDate getData() { return data; }
    public double getTotalVendas() { return totalVendas; }
    public int getTotalIngressos() { return totalIngressos; }
    public int getTotalSessoes() { return totalSessoes; }
    public double getTaxaOcupacaoMedia() { return taxaOcupacaoMedia; }
    public LocalDateTime getMomentoFechamento() { return momentoFechamento; }
}