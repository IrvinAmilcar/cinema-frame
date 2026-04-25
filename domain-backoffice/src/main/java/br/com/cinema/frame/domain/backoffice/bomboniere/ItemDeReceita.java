package br.com.cinema.frame.domain.backoffice.bomboniere;

public class ItemDeReceita {

    private final Insumo insumo;
    private final double quantidade;

    public ItemDeReceita(Insumo insumo, double quantidade) {
        if (insumo == null)
            throw new IllegalArgumentException("Insumo não pode ser nulo");
        if (quantidade <= 0)
            throw new IllegalArgumentException("Quantidade da receita deve ser positiva");

        this.insumo = insumo;
        this.quantidade = quantidade;
    }

    public Insumo getInsumo() { return insumo; }
    public double getQuantidade() { return quantidade; }
}