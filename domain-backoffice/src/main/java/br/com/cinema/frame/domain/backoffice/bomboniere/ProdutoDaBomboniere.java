package br.com.cinema.frame.domain.backoffice.bomboniere;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ProdutoDaBomboniere {

    private final UUID id;
    private final String nome;
    private final double preco;
    private final CategoriaProduto categoria;
    private final List<ItemDeReceita> receita;
    private boolean ativo;

    public ProdutoDaBomboniere(String nome, double preco, CategoriaProduto categoria) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do produto não pode ser vazio");
        if (preco < 0)
            throw new IllegalArgumentException("Preço não pode ser negativo");
        if (categoria == null)
            throw new IllegalArgumentException("Categoria não pode ser nula");

        this.id = UUID.randomUUID();
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
        this.receita = new ArrayList<>();
        this.ativo = true;
    }

    public void adicionarItemReceita(Insumo insumo, double quantidade) {
        receita.add(new ItemDeReceita(insumo, quantidade));
    }

    public void desativar() { this.ativo = false; }
    public void ativar() { this.ativo = true; }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public CategoriaProduto getCategoria() { return categoria; }
    public boolean isAtivo() { return ativo; }
    public List<ItemDeReceita> getReceita() { return Collections.unmodifiableList(receita); }
}