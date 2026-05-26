package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.Insumo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    public double getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }

    public double getNivelCritico() {
        return nivelCritico;
    }

    protected InsumoJpa() {}

    public static InsumoJpa fromDomain(Insumo i) {

        InsumoJpa e = new InsumoJpa();

        e.id = i.getId();
        e.nome = i.getNome();
        e.unidade = i.getUnidade();
        e.setQuantidadeEmEstoque(i.getQuantidadeEmEstoque());
        e.setNivelCritico(i.getNivelCritico());

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

   
public void setQuantidadeEmEstoque(double quantidadeEmEstoque) {
    this.quantidadeEmEstoque = quantidadeEmEstoque;
}

public void setNivelCritico(double nivelCritico) {
    this.nivelCritico = nivelCritico;
}

public void setId(UUID id) { this.id = id; }
public void setNome(String nome) { this.nome = nome; }
public void setUnidade(String unidade) { this.unidade = unidade; }
}