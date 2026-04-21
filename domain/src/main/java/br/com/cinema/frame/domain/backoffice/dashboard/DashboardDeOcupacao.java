package br.com.cinema.frame.domain.backoffice.dashboard;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.precificacao.Precificacao;

import java.util.List;
import java.util.stream.Collectors;

public class DashboardDeOcupacao {

    private final IngressoRepository ingressoRepository;
    private final Precificacao precificacao;

    public DashboardDeOcupacao(IngressoRepository ingressoRepository, Precificacao precificacao) {
        if (ingressoRepository == null)
            throw new IllegalArgumentException("IngressoRepository não pode ser nulo");
        
        if (precificacao == null)
            throw new IllegalArgumentException("Precificacao não pode ser nula");

        this.ingressoRepository = ingressoRepository;
        this.precificacao = precificacao;
    }

    public OcupacaoDaSessao calcularOcupacao(Sessao sessao) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");

        List<Ingresso> ingressosVendidos = ingressoRepository.buscarPorSessao(sessao);

        int assentosVendidos = ingressosVendidos.size();
        int totalAssentos = sessao.getSala().getCapacidade();
        double precoPorIngresso = precificacao.calcularPreco(sessao);

        double faturamentoProjetado = totalAssentos * precoPorIngresso;
        double faturamentoRealizado = assentosVendidos * precoPorIngresso;

        return new OcupacaoDaSessao(sessao, assentosVendidos,
            faturamentoProjetado, faturamentoRealizado);
    }

    public List<OcupacaoDaSessao> calcularOcupacaoDaSemana(List<Sessao> sessoes) {
        if (sessoes == null)
            throw new IllegalArgumentException("Lista de sessões não pode ser nula");

        return sessoes.stream()
            .map(this::calcularOcupacao)
            .collect(Collectors.toList());
    }
}