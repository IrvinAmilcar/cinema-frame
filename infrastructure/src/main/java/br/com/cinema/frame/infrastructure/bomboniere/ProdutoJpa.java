package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    protected ProdutoJpa() {}

    public static ProdutoJpa fromDomain(
            ProdutoDaBomboniere p
    ) {

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
                Enum.valueOf(
                        br.com.cinema.frame.domain.backoffice.bomboniere.CategoriaProduto.class,
                        categoria
                ),
                List.of(),
                ativo
        );
    }
}