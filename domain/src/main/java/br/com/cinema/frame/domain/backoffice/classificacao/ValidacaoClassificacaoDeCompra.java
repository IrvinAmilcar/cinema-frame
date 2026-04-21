package br.com.cinema.frame.domain.backoffice.classificacao;

import br.com.cinema.frame.domain.portal.cliente.Cliente;
import br.com.cinema.frame.domain.backoffice.grade.Filme;

public class ValidacaoClassificacaoDeCompra {

    private final ValidacaoClassificacao validacaoClassificacao = new ValidacaoClassificacao();

    public void validarCompra(Cliente cliente, Filme filme) {
        if (cliente == null)
            throw new IllegalArgumentException("Cliente não pode ser nulo");
        if (filme == null)
            throw new IllegalArgumentException("Filme não pode ser nulo");

        boolean permitido = validacaoClassificacao.validar(
            cliente.getDataNascimento(),
            filme.getClassificacaoIndicativa()
        );

        if (!permitido)
            throw new IllegalStateException(
                "Cliente " + cliente.getNome() +
                " não tem idade permitida para assistir este filme. " +
                "Classificação: " + filme.getClassificacaoIndicativa()
            );
    }
}