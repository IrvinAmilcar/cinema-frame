package br.com.cinema.frame.domain.backoffice.caixa;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

public class Bordero {

    private static final double PERCENTUAL_DISTRIBUIDORA = 0.50;

    private final Sessao sessao;
    private final int quantidadeInteira;
    private final int quantidadeMeia;
    private final int quantidadeConvite;
    private final double totalArrecadado;

    public Bordero(Sessao sessao, int quantidadeInteira, int quantidadeMeia, int quantidadeConvite, double totalArrecadado) {
        this.sessao = sessao;
        this.quantidadeInteira = quantidadeInteira;
        this.quantidadeMeia = quantidadeMeia;
        this.quantidadeConvite = quantidadeConvite;
        this.totalArrecadado = totalArrecadado;
    }

    public double getRepasseDistribuidora() {
        return totalArrecadado * PERCENTUAL_DISTRIBUIDORA;
    }

    public double getReceitaCinema() {
        return totalArrecadado - getRepasseDistribuidora();
    }

    public Sessao getSessao() { return sessao; }
    public int getQuantidadeInteira() { return quantidadeInteira; }
    public int getQuantidadeMeia() { return quantidadeMeia; }
    public int getQuantidadeConvite() { return quantidadeConvite; }
    public double getTotalArrecadado() { return totalArrecadado; }
    public int getTotalIngressos() { return quantidadeInteira + quantidadeMeia + quantidadeConvite; }
}