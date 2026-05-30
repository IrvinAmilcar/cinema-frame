package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;

public class LancamentoPontos {

    private int saldo;
    private final int pontosOriginais;
    private final LocalDate validade;
    private final LocalDate dataCriacao;
    private boolean expirado;
    private final String descricao;

    public LancamentoPontos(int saldo, LocalDate validade, LocalDate dataCriacao) {
        this(saldo, validade, dataCriacao, "Compra de ingresso");
    }

    public LancamentoPontos(int saldo, LocalDate validade, LocalDate dataCriacao, String descricao) {
        if (saldo <= 0) throw new IllegalArgumentException("Saldo deve ser positivo");
        if (validade == null) throw new IllegalArgumentException("Validade é obrigatória");
        if (dataCriacao == null) throw new IllegalArgumentException("Data de criação é obrigatória");
        this.saldo = saldo;
        this.pontosOriginais = saldo;
        this.validade = validade;
        this.dataCriacao = dataCriacao;
        this.expirado = false;
        this.descricao = descricao != null ? descricao : "Compra de ingresso";
    }

    public void debitar(int pontos) {
        if (pontos > saldo) throw new IllegalStateException("Saldo do lançamento insuficiente");
        saldo -= pontos;
    }

    public void expirar() { this.expirado = true; this.saldo = 0; }

    public boolean isExpirado() { return expirado; }
    public int getSaldo() { return saldo; }
    public int getPontosOriginais() { return pontosOriginais; }
    public LocalDate getValidade() { return validade; }
    public LocalDate getDataCriacao() { return dataCriacao; }
    public String getDescricao() { return descricao; }
}