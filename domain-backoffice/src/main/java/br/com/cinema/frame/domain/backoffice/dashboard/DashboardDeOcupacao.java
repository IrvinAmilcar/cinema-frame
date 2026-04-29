package br.com.cinema.frame.domain.backoffice.dashboard;

import java.util.List;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.precificacao.PrecificacaoService;

public class DashboardDeOcupacao {

    private final IngressoRepository ingressoRepository;
    private final PrecificacaoService precificacaoService;

    public DashboardDeOcupacao(IngressoRepository ingressoRepository,
                                PrecificacaoService precificacaoService) {
        if (ingressoRepository == null)
            throw new IllegalArgumentException("IngressoRepository não pode ser nulo");
        if (precificacaoService == null)
            throw new IllegalArgumentException("PrecificacaoService não pode ser nulo");

        this.ingressoRepository = ingressoRepository;
        this.precificacaoService = precificacaoService;
    }

    public OcupacaoDaSessao calcularOcupacao(Sessao sessao) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");

        List<Ingresso> ingressos = ingressoRepository.buscarPorSessao(sessao);

        double precoUnitario = precificacaoService.calcularPreco(sessao);
        int capacidade = sessao.getSala().getCapacidade();

        // L7: multiplica pelo fator do tipo (INTEIRA=1.0, MEIA=0.5, CONVITE=0.0)
        double faturamentoRealizado = ingressos.stream()
            .mapToDouble(i -> precoUnitario * i.getTipo().getFatorPreco())
            .sum();
        double faturamentoProjetado = capacidade * precoUnitario;

        return new OcupacaoDaSessao(sessao, ingressos.size(),
            faturamentoRealizado, faturamentoProjetado);
    }

    public List<OcupacaoDaSessao> calcularOcupacaoDaSemana(List<Sessao> sessoes) {
        if (sessoes == null)
            throw new IllegalArgumentException("Lista de sessões não pode ser nula");

        return sessoes.stream()
            .map(this::calcularOcupacao)
            .toList();
    }
}