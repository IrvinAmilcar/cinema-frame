package br.com.cinema.frame.domain.backoffice.bomboniere;

import java.util.UUID;

public class Insumo {

    private final UUID id;
    private final String nome;
    private final String unidade;
    private double quantidadeEmEstoque;
    private final double nivelCritico;

    public Insumo(String nome, String unidade, double quantidadeEmEstoque, double nivelCritico) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do insumo não pode ser vazio");
        if (quantidadeEmEstoque < 0)
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa");
        if (nivelCritico < 0)
            throw new IllegalArgumentException("Nível crítico não pode ser negativo");

        this.id = UUID.randomUUID();
        this.nome = nome;
        this.unidade = unidade;
        this.quantidadeEmEstoque = quantidadeEmEstoque;
        this.nivelCritico = nivelCritico;
    }

    public void baixar(double quantidade) {
        if (quantidade <= 0)
            throw new IllegalArgumentException("Quantidade para baixa deve ser positiva");
        if (quantidade > quantidadeEmEstoque)
            throw new IllegalStateException("Estoque insuficiente para o insumo: " + nome);

        this.quantidadeEmEstoque -= quantidade;
    }

    public boolean isEstoqueCritico() {
        return quantidadeEmEstoque <= nivelCritico;
    }

    public void repor(double quantidade) {
    if (quantidade <= 0)
        throw new IllegalArgumentException("Quantidade para reposição deve ser positiva");
    this.quantidadeEmEstoque += quantidade;
}

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getUnidade() { return unidade; }
    public double getQuantidadeEmEstoque() { return quantidadeEmEstoque; }
    public double getNivelCritico() { return nivelCritico; }
}