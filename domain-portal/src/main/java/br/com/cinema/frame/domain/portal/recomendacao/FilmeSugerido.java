package br.com.cinema.frame.domain.portal.recomendacao;

import java.util.UUID;

import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

public class FilmeSugerido {

    private final UUID id;
    private final String titulo;
    private final GeneroFilme genero;

    public FilmeSugerido(String titulo, GeneroFilme genero) {
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("Título do filme sugerido não pode ser vazio");
        if (genero == null)
            throw new IllegalArgumentException("Gênero do filme sugerido não pode ser nulo");

        this.id = UUID.randomUUID();
        this.titulo = titulo;
        this.genero = genero;
    }

    private FilmeSugerido(UUID id, String titulo, GeneroFilme genero) {
        this.id = id;
        this.titulo = titulo;
        this.genero = genero;
    }

    public static FilmeSugerido reconstituir(UUID id, String titulo, GeneroFilme genero) {
        if (id == null) throw new IllegalArgumentException("ID não pode ser nulo");
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("Título não pode ser vazio");
        if (genero == null) throw new IllegalArgumentException("Gênero não pode ser nulo");
        return new FilmeSugerido(id, titulo, genero);
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public GeneroFilme getGenero() { return genero; }
}
