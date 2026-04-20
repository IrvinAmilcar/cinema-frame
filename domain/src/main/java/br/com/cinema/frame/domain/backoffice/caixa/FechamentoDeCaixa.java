package br.com.cinema.frame.domain.backoffice.caixa;

import br.com.cinema.frame.domain.backoffice.grade.Ingresso;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.grade.TipoIngresso;
import br.com.cinema.frame.domain.backoffice.precificacao.Precificacao;

import java.util.List;

public class FechamentoDeCaixa {

    private final Precificacao precificacao = new Precificacao();

    public Bordero gerarBordero(Sessao sessao, List<Ingresso> ingressos) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");
        if (ingressos == null || ingressos.isEmpty())
            throw new IllegalArgumentException("Lista de ingressos não pode ser nula ou vazia");

        double precoBase = precificacao.calcularPreco(sessao);

        int inteiras = 0, meias = 0, convites = 0;
        double total = 0.0;

        for (Ingresso ingresso : ingressos) {
            if (!ingresso.getSessao().getId().equals(sessao.getId()))
                throw new IllegalStateException("Ingresso não pertence a esta sessão");

            TipoIngresso tipo = ingresso.getTipo();
            total += precoBase * tipo.getFatorPreco();

            switch (tipo) {
                case INTEIRA  -> inteiras++;
                case MEIA     -> meias++;
                case CONVITE  -> convites++;
            }
        }

        return new Bordero(sessao, inteiras, meias, convites, total);
    }
}