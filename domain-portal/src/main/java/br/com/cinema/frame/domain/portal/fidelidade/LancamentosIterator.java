package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class LancamentosIterator implements Iterator<LancamentoPontos> {

    private final List<LancamentoPontos> lancamentos;
    private final LocalDate hoje;
    private int cursor = 0;
    private LancamentoPontos proximo = null;

    public LancamentosIterator(List<LancamentoPontos> lancamentos, LocalDate hoje) {
        this.lancamentos = lancamentos;
        this.hoje = hoje;
        avancar();
    }

    private void avancar() {
        proximo = null;
        while (cursor < lancamentos.size()) {
            LancamentoPontos l = lancamentos.get(cursor++);
            if (!l.isExpirado() && !l.getValidade().isBefore(hoje)) {
                proximo = l;
                break;
            }
        }
    }

    @Override
    public boolean hasNext() { return proximo != null; }

    @Override
    public LancamentoPontos next() {
        if (proximo == null) throw new NoSuchElementException();
        LancamentoPontos atual = proximo;
        avancar();
        return atual;
    }
}
