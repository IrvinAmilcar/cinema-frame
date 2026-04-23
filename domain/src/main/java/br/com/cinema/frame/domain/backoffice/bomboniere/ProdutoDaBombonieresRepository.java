package br.com.cinema.frame.domain.backoffice.bomboniere;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoDaBombonieresRepository {

    void salvar(ProdutoDaBomboniere produto);
    Optional<ProdutoDaBomboniere> buscarPorId(UUID id);
    Optional<ProdutoDaBomboniere> buscarPorNome(String nome);
    List<ProdutoDaBomboniere> listarTodos();
    void remover(UUID id);
}