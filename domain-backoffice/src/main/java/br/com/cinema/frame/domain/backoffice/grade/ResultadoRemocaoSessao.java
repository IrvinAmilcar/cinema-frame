package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;

import java.util.Collections;
import java.util.List;

public class ResultadoRemocaoSessao {

    private final Sessao sessao;
    private final List<Ingresso> ingressosParaReembolso;

    public ResultadoRemocaoSessao(Sessao sessao, List<Ingresso> ingressosParaReembolso) {
        this.sessao = sessao;
        this.ingressosParaReembolso = Collections.unmodifiableList(ingressosParaReembolso);
    }

    public Sessao getSessao() { return sessao; }
    public List<Ingresso> getIngressosParaReembolso() { return ingressosParaReembolso; }
}
