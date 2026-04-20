package br.com.cinema.frame.domain.backoffice.ingresso;

public enum TipoIngresso {

    INTEIRA(1.0),
    MEIA(0.5),
    CONVITE(0.0);

    private final double fatorPreco;

    TipoIngresso(double fatorPreco) {
        this.fatorPreco = fatorPreco;
    }

    public double getFatorPreco() {
        return fatorPreco;
    }
}