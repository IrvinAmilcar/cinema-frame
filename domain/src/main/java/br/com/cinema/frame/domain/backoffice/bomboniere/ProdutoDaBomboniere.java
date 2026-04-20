package br.com.cinema.frame.domain.backoffice.bomboniere;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ProdutoDaBomboniere {

    private final UUID id;
    private final String nome;
    private final List<ItemDeReceita> receita;

    public ProdutoDaBomboniere(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do produto não pode ser vazio");

        this.id = UUID.randomUUID();
        this.nome = nome;
        this.receita = new ArrayList<>();
    }

    public void adicionarItemReceita(Insumo insumo, double quantidade) {
        receita.add(new ItemDeReceita(insumo, quantidade));
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public List<ItemDeReceita> getReceita() { return Collections.unmodifiableList(receita); }
}