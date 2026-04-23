package br.com.cinema.frame.domain.backoffice.bomboniere;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsumoRepository {

    void salvar(Insumo insumo);
    Optional<Insumo> buscarPorId(UUID id);
    Optional<Insumo> buscarPorNome(String nome);
    List<Insumo> listarTodos();
    List<Insumo> listarEstoqueCritico();
}