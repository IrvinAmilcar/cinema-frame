package br.com.cinema.frame.domain.portal.recomendacao;

import java.util.List;

public interface FilmeSugeridoRepository {

    void salvar(FilmeSugerido filme);
    List<FilmeSugerido> listarTodos();
}