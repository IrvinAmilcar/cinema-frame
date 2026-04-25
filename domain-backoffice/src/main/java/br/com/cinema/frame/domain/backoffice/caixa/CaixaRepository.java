package br.com.cinema.frame.domain.backoffice.caixa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaixaRepository {

    void salvar(Bordero bordero);
    Optional<Bordero> buscarPorSessaoId(UUID sessaoId);
    List<Bordero> listarTodos();
}