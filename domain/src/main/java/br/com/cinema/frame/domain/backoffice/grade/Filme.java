package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoIndicativa;
import java.time.Duration;
import java.util.UUID;

public class Filme {

    private UUID id;
    private String titulo;
    private Duration duracao;
    private ClassificacaoIndicativa classificacaoIndicativa;

    public Filme(String titulo, Duration duracao, ClassificacaoIndicativa classificacaoIndicativa) {
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("Título do filme não pode ser vazio");
        if (duracao == null || duracao.isNegative() || duracao.isZero())
            throw new IllegalArgumentException("Duração do filme deve ser positiva");
        if (classificacaoIndicativa == null)
            throw new IllegalArgumentException("Classificação indicativa não pode ser nula");

        this.id = UUID.randomUUID();
        this.titulo = titulo;
        this.duracao = duracao;
        this.classificacaoIndicativa = classificacaoIndicativa;
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public Duration getDuracao() { return duracao; }
    public ClassificacaoIndicativa getClassificacaoIndicativa() { return classificacaoIndicativa; }
}