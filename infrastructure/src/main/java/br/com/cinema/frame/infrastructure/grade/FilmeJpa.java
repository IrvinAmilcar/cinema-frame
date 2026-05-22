package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;
import jakarta.persistence.*;

import java.time.Duration;
import java.util.UUID;

@Entity
@Table(name = "filmes")
public class FilmeJpa {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private long duracaoSegundos;

    @Column(nullable = false)
    private String classificacaoIndicativa;

    @Column(nullable = false)
    private String genero;

    private String trailerURL;

    @Column(nullable = false)
    private boolean ativo;

    protected FilmeJpa() {}

    public static FilmeJpa fromDomain(Filme f) {
        FilmeJpa e = new FilmeJpa();
        e.id = f.getId();
        e.titulo = f.getTitulo();
        e.duracaoSegundos = f.getDuracao().getSeconds();
        e.classificacaoIndicativa = f.getClassificacaoIndicativa().name();
        e.genero = f.getGenero().name();
        e.trailerURL = f.getTrailerURL();
        e.ativo = f.isAtivo();
        return e;
    }

    public Filme toDomain() {
        return Filme.reconstituir(
            id,
            titulo,
            Duration.ofSeconds(duracaoSegundos),
            ClassificacaoIndicativa.valueOf(classificacaoIndicativa),
            GeneroFilme.valueOf(genero),
            trailerURL,
            ativo
        );
    }

    public UUID getId() { return id; }
}
