package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.time.Duration;
import java.util.UUID;

public class Filme {

    private UUID id;
    private String titulo;
    private Duration duracao;
    private ClassificacaoIndicativa classificacaoIndicativa;
    private GeneroFilme genero;
    private String trailerURL;
    private boolean ativo;

    public Filme(String titulo, Duration duracao, ClassificacaoIndicativa classificacaoIndicativa, GeneroFilme genero) {
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("Título do filme não pode ser vazio");
        if (duracao == null || duracao.isNegative() || duracao.isZero())
            throw new IllegalArgumentException("Duração do filme deve ser positiva");
        if (classificacaoIndicativa == null)
            throw new IllegalArgumentException("Classificação indicativa não pode ser nula");
        if (genero == null)
            throw new IllegalArgumentException("Gênero do filme não pode ser nulo");

        this.id = UUID.randomUUID();
        this.titulo = titulo;
        this.duracao = duracao;
        this.classificacaoIndicativa = classificacaoIndicativa;
        this.genero = genero;
        this.ativo = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filme filme = (Filme) o;
        return titulo.equals(filme.titulo);
    }

    @Override
    public int hashCode() {
        return titulo.hashCode();
    }

    public void atualizar(String novoTitulo, Duration novaDuracao,
                          ClassificacaoIndicativa novaClassificacao, GeneroFilme novoGenero, String novoTrailerURL) {
        if (novoTitulo != null && !novoTitulo.isBlank()) this.titulo = novoTitulo;
        if (novaDuracao != null && !novaDuracao.isNegative() && !novaDuracao.isZero()) this.duracao = novaDuracao;
        if (novaClassificacao != null) this.classificacaoIndicativa = novaClassificacao;
        if (novoGenero != null) this.genero = novoGenero;
        if (novoTrailerURL != null && !novoTrailerURL.isBlank()) this.trailerURL = novoTrailerURL;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public Duration getDuracao() { return duracao; }
    public ClassificacaoIndicativa getClassificacaoIndicativa() { return classificacaoIndicativa; }
    public GeneroFilme getGenero() { return genero; }
    public String getTrailerURL() { return trailerURL; }
    public boolean isAtivo() { return ativo; }
}
