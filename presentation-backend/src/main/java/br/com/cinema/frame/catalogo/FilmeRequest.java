package br.com.cinema.frame.catalogo;

public record FilmeRequest(
    String titulo,
    int duracaoMinutos,
    String classificacaoIndicativa,
    String genero,
    String trailerURL
) {}
