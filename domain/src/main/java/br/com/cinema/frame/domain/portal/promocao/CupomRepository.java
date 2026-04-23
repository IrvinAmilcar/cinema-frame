package br.com.cinema.frame.domain.portal.promocao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CupomRepository {

    void salvar(Cupom cupom);
    Optional<Cupom> buscarPorId(UUID id);
    Optional<Cupom> buscarPorCodigo(String codigo);
    List<Cupom> listarTodos();
    void remover(UUID id);
}