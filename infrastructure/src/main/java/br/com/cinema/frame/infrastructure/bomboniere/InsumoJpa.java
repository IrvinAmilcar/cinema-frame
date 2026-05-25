package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.Insumo;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "insumos")
public class InsumoJpa {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String unidade;

    @Column(nullable = false)
    private double quantidadeEmEstoque;

    @Column(nullable = false)
    private double nivelCritico;

    @Version
    private Long version;

    protected InsumoJpa() {}

    public static InsumoJpa fromDomain(Insumo i) {
        InsumoJpa e = new InsumoJpa();

        e.id = i.getId();
        e.nome = i.getNome();
        e.unidade = i.getUnidade();
        e.quantidadeEmEstoque = i.getQuantidadeEmEstoque();
        e.nivelCritico = i.getNivelCritico();

        return e;
    }

    public Insumo toDomain() {
        return Insumo.reconstituir(
                id,
                nome,
                unidade,
                quantidadeEmEstoque,
                nivelCritico
        );
    }

    public UUID getId() {
        return id;
    }
}