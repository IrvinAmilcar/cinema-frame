package br.com.cinema.frame.domain.backoffice.dashboard;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

public class OcupacaoDaSessao {

    private final Sessao sessao;
    private final int totalAssentos;
    private final int assentosVendidos;
    private final double faturamentoProjetado;
    private final double faturamentoRealizado;

    public OcupacaoDaSessao(Sessao sessao, int assentosVendidos,
                             double faturamentoProjetado, double faturamentoRealizado) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");
        if (assentosVendidos < 0)
            throw new IllegalArgumentException("Assentos vendidos não pode ser negativo");
        if (assentosVendidos > sessao.getSala().getCapacidade())
            throw new IllegalArgumentException("Assentos vendidos não pode ser maior que a capacidade da sala");

        this.sessao = sessao;
        this.totalAssentos = sessao.getSala().getCapacidade();
        this.assentosVendidos = assentosVendidos;
        this.faturamentoProjetado = faturamentoProjetado;
        this.faturamentoRealizado = faturamentoRealizado;
    }

    public double getTaxaDeOcupacao() {
        if (totalAssentos == 0) return 0;
        return (double) assentosVendidos / totalAssentos * 100;
    }

    public Sessao getSessao() { return sessao; }
    public int getTotalAssentos() { return totalAssentos; }
    public int getAssentosVendidos() { return assentosVendidos; }
    public double getFaturamentoProjetado() { return faturamentoProjetado; }
    public double getFaturamentoRealizado() { return faturamentoRealizado; }
}