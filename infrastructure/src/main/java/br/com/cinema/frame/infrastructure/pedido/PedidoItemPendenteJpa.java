package br.com.cinema.frame.infrastructure.pedido;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedido_itens_pendentes")
public class PedidoItemPendenteJpa {

    @Id
    private UUID id;

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Column(name = "produto_id", nullable = false)
    private UUID produtoId;

    @Column(name = "nome_produto", nullable = false)
    private String nomeProduto;

    @Column(nullable = false)
    private double preco;

    protected PedidoItemPendenteJpa() {}

    public PedidoItemPendenteJpa(UUID pedidoId, UUID produtoId, String nomeProduto, double preco) {
        this.id = UUID.randomUUID();
        this.pedidoId = pedidoId;
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.preco = preco;
    }

    public UUID getId() { return id; }
    public UUID getPedidoId() { return pedidoId; }
    public UUID getProdutoId() { return produtoId; }
    public String getNomeProduto() { return nomeProduto; }
    public double getPreco() { return preco; }
}
