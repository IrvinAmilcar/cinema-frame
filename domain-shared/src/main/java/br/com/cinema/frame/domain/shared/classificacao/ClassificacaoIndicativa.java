package br.com.cinema.frame.domain.shared.classificacao;

public enum ClassificacaoIndicativa {

    LIVRE(0),
    DEZ(10),
    DOZE(12),
    QUATORZE(14),
    DEZESSEIS(16),
    DEZOITO(18);

    private final int idadeMinima;

    ClassificacaoIndicativa(int idadeMinima) {
        this.idadeMinima = idadeMinima;
    }

    public int getIdadeMinima() {
        return idadeMinima;
    }

    public boolean isLivre() {
        return this == LIVRE;
    }
}
