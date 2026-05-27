package br.com.cinema.frame.domain.backoffice.bomboniere;

public interface EstoqueSubject {
    void adicionarObservador(EstoqueObserver observer);
    void removerObservador(EstoqueObserver observer);
    void notificarObservadores(Insumo insumo);
}
