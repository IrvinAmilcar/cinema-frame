package br.com.cinema.frame.infrastructure.programacao;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.portal.recomendacao.FilmeSugerido;
import br.com.cinema.frame.domain.portal.recomendacao.FilmeSugeridoRepository;
import br.com.cinema.frame.infrastructure.grade.FilmeJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FilmeSugeridoRepositoryAdapter implements FilmeSugeridoRepository {

    private final FilmeJpaRepository jpa;

    public FilmeSugeridoRepositoryAdapter(FilmeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(FilmeSugerido filme) {
        // Sugestões derivam do catálogo ativo — sem persistência separada
    }

    @Override
    public List<FilmeSugerido> listarTodos() {
        return jpa.findByAtivo(true).stream()
                .map(f -> {
                    Filme dominio = f.toDomain();
                    return FilmeSugerido.reconstituir(dominio.getId(), dominio.getTitulo(), dominio.getGenero());
                })
                .toList();
    }
}
