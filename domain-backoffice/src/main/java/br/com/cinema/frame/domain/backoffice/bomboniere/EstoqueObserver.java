package br.com.cinema.frame.domain.backoffice.bomboniere;

public interface EstoqueObserver {
    void notificarEstoqueCritico(Insumo insumo);
}