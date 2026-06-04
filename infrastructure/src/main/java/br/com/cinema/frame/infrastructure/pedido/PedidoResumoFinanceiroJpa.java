package br.com.cinema.frame.infrastructure.pedido;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedido_resumo_financeiro")
public class PedidoResumoFinanceiroJpa {

    @Id
    private UUID id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private UUID pedidoId;

    @Column(name = "valor_ingressos", nullable = false)
    private double valorIngressos;

    @Column(name = "valor_bomboniere", nullable = false)
    private double valorBomboniere;

    @Column(name = "desconto_cupom", nullable = false)
    private double descontoCupom;

    @Column(name = "desconto_pontos", nullable = false)
    private double descontoPontos;

    @Column(name = "valor_total", nullable = false)
    private double valorTotal;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    protected PedidoResumoFinanceiroJpa() {}

    public PedidoResumoFinanceiroJpa(UUID pedidoId, double valorIngressos, double valorBomboniere,
                                      double descontoCupom, double descontoPontos,
                                      double valorTotal, LocalDate dataPedido) {
        this.id = UUID.randomUUID();
        this.pedidoId = pedidoId;
        this.valorIngressos = valorIngressos;
        this.valorBomboniere = valorBomboniere;
        this.descontoCupom = descontoCupom;
        this.descontoPontos = descontoPontos;
        this.valorTotal = valorTotal;
        this.dataPedido = dataPedido;
    }

    public UUID getId() { return id; }
    public UUID getPedidoId() { return pedidoId; }
    public double getValorIngressos() { return valorIngressos; }
    public double getValorBomboniere() { return valorBomboniere; }
    public double getDescontoCupom() { return descontoCupom; }
    public double getDescontoPontos() { return descontoPontos; }
    public double getValorTotal() { return valorTotal; }
    public LocalDate getDataPedido() { return dataPedido; }
}
