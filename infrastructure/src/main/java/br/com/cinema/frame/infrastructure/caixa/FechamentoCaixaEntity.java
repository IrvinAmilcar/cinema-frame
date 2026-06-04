package br.com.cinema.frame.infrastructure.caixa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fechamentos_caixa")
public class FechamentoCaixaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private LocalDate data;

    @Column(name = "total_vendas", nullable = false)
    private double totalVendas;

    @Column(name = "total_ingressos", nullable = false)
    private int totalIngressos;

    @Column(name = "total_sessoes", nullable = false)
    private int totalSessoes;

    @Column(name = "taxa_ocupacao_media", nullable = false)
    private double taxaOcupacaoMedia;

    @Column(name = "momento_fechamento", nullable = false)
    private LocalDateTime momentoFechamento;

    public FechamentoCaixaEntity() {}

    public FechamentoCaixaEntity(UUID id, LocalDate data, double totalVendas,
                                  int totalIngressos, int totalSessoes,
                                  double taxaOcupacaoMedia, LocalDateTime momentoFechamento) {
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
