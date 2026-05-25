package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.CategoriaProduto;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "produtos_bomboniere")
public class ProdutoJpa {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private double preco;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private boolean ativo;

    @Version
    private Long version;

    protected ProdutoJpa() {}

    public static ProdutoJpa fromDomain(ProdutoDaBomboniere p) {
        ProdutoJpa e = new ProdutoJpa();

        e.id = p.getId();
        e.nome = p.getNome();
        e.preco = p.getPreco();
        e.categoria = p.getCategoria().name();
        e.ativo = p.isAtivo();

        return e;
    }

    public ProdutoDaBomboniere toDomain() {
        return ProdutoDaBomboniere.reconstituir(
                id,
                nome,
                preco,
                CategoriaProduto.valueOf(categoria),
                new ArrayList<>(),
                ativo
        );
    }

    public UUID getId() {
        return id;
    }
}