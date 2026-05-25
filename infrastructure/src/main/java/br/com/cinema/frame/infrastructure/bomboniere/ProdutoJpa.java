package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.CategoriaProduto;
import br.com.cinema.frame.domain.backoffice.bomboniere.ItemDeReceita;
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

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "produto_id")
    private List<ItemDeReceitaJpa> receita = new ArrayList<>();

    protected ProdutoJpa() {
    }

    public static ProdutoJpa fromDomain(
            ProdutoDaBomboniere produto
    ) {

        ProdutoJpa entity = new ProdutoJpa();

        entity.id = produto.getId();
        entity.nome = produto.getNome();
        entity.preco = produto.getPreco();
        entity.categoria = produto.getCategoria().name();
        entity.ativo = produto.isAtivo();

        entity.receita =
                produto.getReceita()
                        .stream()
                        .map(item -> new ItemDeReceitaJpa(
                                InsumoJpa.fromDomain(item.getInsumo()),
                                item.getQuantidade()
                        ))
                        .toList();

        return entity;
    }

    public ProdutoDaBomboniere toDomain() {

        ProdutoDaBomboniere produto =
                ProdutoDaBomboniere.reconstituir(
                        id,
                        nome,
                        preco,
                        CategoriaProduto.valueOf(categoria),
                        new ArrayList<>(),
                        ativo
                );

        for (ItemDeReceitaJpa item : receita) {

            produto.adicionarItemReceita(
                    item.getInsumo().toDomain(),
                    item.getQuantidade()
            );
        }

        return produto;
    }
}