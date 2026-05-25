package br.com.cinema.frame.infrastructure.bomboniere;

import jakarta.persistence.*;

@Entity
@Table(name = "itens_receita")
public class ItemDeReceitaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "insumo_id")
    private InsumoJpa insumo;

    @Column(nullable = false)
    private double quantidade;

    protected ItemDeReceitaJpa() {
    }

    public ItemDeReceitaJpa(
            InsumoJpa insumo,
            double quantidade
    ) {
        this.insumo = insumo;
        this.quantidade = quantidade;
    }

    public InsumoJpa getInsumo() {
        return insumo;
    }

    public double getQuantidade() {
        return quantidade;
    }
}