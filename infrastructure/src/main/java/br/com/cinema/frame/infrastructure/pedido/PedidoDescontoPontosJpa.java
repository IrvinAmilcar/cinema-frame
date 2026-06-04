package br.com.cinema.frame.infrastructure.pedido;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedido_desconto_pontos")
public class PedidoDescontoPontosJpa {

    @Id
    private UUID id;

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Column(name = "valor_desconto", nullable = false)
    private double valorDesconto;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    protected PedidoDescontoPontosJpa() {}

    public PedidoDescontoPontosJpa(UUID pedidoId, double valorDesconto, LocalDate dataPedido) {
        this.id = UUID.randomUUID();
        this.pedidoId = pedidoId;
        this.valorDesconto = valorDesconto;
        this.dataPedido = dataPedido;
    }

    public UUID getId() { return id; }
    public UUID getPedidoId() { return pedidoId; }
    public double getValorDesconto() { return valorDesconto; }
    public LocalDate getDataPedido() { return dataPedido; }
}