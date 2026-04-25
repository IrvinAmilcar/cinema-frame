package br.com.cinema.frame.domain.shared.filme;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilmeRepository {

    void salvar(Filme filme);
    Optional<Filme> buscarPorId(UUID id);
    List<Filme> listarTodos();
    void remover(UUID id);
}
