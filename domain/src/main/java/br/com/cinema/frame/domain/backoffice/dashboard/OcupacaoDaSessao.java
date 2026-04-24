package br.com.cinema.frame.domain.backoffice.dashboard;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

public class OcupacaoDaSessao {

    private final Sessao sessao;
    private final int ingressosVendidos;
    private final double faturamentoRealizado;
    private final double faturamentoProjetado;

    public OcupacaoDaSessao(Sessao sessao, int ingressosVendidos,
                             double faturamentoRealizado, double faturamentoProjetado) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");
        if (ingressosVendidos < 0)
            throw new IllegalArgumentException("Ingressos vendidos não pode ser negativo");
        if (faturamentoRealizado < 0)
            throw new IllegalArgumentException("Faturamento realizado não pode ser negativo");
        if (faturamentoProjetado < 0)
            throw new IllegalArgumentException("Faturamento projetado não pode ser negativo");

        this.sessao = sessao;
        this.ingressosVendidos = ingressosVendidos;
        this.faturamentoRealizado = faturamentoRealizado;
        this.faturamentoProjetado = faturamentoProjetado;
    }

    public double getTaxaDeOcupacao() {
        int capacidade = sessao.getSala().getCapacidade();
        if (capacidade == 0)
            return 0.0;
        return ((double) ingressosVendidos / capacidade) * 100.0;
    }

    public Sessao getSessao() { return sessao; }
    public int getIngressosVendidos() { return ingressosVendidos; }
    public double getFaturamentoRealizado() { return faturamentoRealizado; }
    public double getFaturamentoProjetado() { return faturamentoProjetado; }
}