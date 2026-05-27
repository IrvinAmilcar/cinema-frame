package br.com.cinema.frame.programacao;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

import java.util.UUID;

public record SessaoDisponivelResponse(
        UUID id,
        String filme,
        String genero,
        String classificacao,
        String inicio,
        int capacidade
) {
    public static SessaoDisponivelResponse from(Sessao s) {
        return new SessaoDisponivelResponse(
                s.getId(),
                s.getFilme().getTitulo(),
                s.getFilme().getGenero().name(),
                s.getFilme().getClassificacaoIndicativa().name(),
                s.getInicio().toString(),
                s.getSala().getCapacidade()
        );
    }
}
