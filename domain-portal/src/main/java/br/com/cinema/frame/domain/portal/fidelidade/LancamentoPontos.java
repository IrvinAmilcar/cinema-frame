package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;

public class LancamentoPontos {

    private int saldo;
    private final LocalDate validade;
    private boolean expirado;

    public LancamentoPontos(int saldo, LocalDate validade) {
        this.saldo = saldo;
        this.validade = validade;
        this.expirado = false;
    }

    public void debitar(int pontos) {
        if (pontos > saldo) throw new IllegalStateException("Saldo do lançamento insuficiente");
        saldo -= pontos;
    }

    public void expirar() { this.expirado = true; this.saldo = 0; }
    public boolean isExpirado() { return expirado; }
    public int getSaldo() { return saldo; }
    public LocalDate getValidade() { return validade; }
}
