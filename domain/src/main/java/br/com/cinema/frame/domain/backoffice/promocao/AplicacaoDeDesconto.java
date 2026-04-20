package br.com.cinema.frame.domain.backoffice.promocao;

public class AplicacaoDeDesconto {

    private final double valorOriginal;
    private final double valorDesconto;
    private final double valorFinal;

    public AplicacaoDeDesconto(double valorOriginal, double valorDesconto) {
        if (valorOriginal < 0)
            throw new IllegalArgumentException("Valor original não pode ser negativo");
        if (valorDesconto < 0)
            throw new IllegalArgumentException("Valor de desconto não pode ser negativo");
        if (valorDesconto > valorOriginal)
            throw new IllegalArgumentException("Desconto não pode ser maior que o valor original");

        this.valorOriginal = valorOriginal;
        this.valorDesconto = valorDesconto;
        this.valorFinal = valorOriginal - valorDesconto;
    }

    public double getValorOriginal() { return valorOriginal; }
    public double getValorDesconto() { return valorDesconto; }
    public double getValorFinal() { return valorFinal; }
}