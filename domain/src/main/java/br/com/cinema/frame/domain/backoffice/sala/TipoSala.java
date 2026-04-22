package br.com.cinema.frame.domain.backoffice.sala;

public enum TipoSala {

    PADRAO(0.0),
    TRES_D(0.50),
    IMAX(1.20),
    VIP(2.00);

    private final double percentualAcrescimo;

    TipoSala(double percentualAcrescimo) {
        this.percentualAcrescimo = percentualAcrescimo;
    }

    public double getPercentualAcrescimo() {
        return percentualAcrescimo;
    }
}