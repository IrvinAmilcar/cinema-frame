package br.com.cinema.frame.catalogo;

import br.com.cinema.frame.domain.backoffice.grade.Filme;

import java.util.UUID;

public record FilmeResponse(
    UUID id,
    String titulo,
    int duracaoMinutos,
    String classificacaoIndicativa,
    String genero,
    String trailerURL,
    boolean ativo
) {
    public static FilmeResponse from(Filme f) {
        return new FilmeResponse(
            f.getId(),
            f.getTitulo(),
            (int) f.getDuracao().toMinutes(),
            f.getClassificacaoIndicativa().name(),
            f.getGenero().name(),
            f.getTrailerURL(),
            f.isAtivo()
        );
    }
}
