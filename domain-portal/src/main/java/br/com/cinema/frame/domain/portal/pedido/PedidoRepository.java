package br.com.cinema.frame.domain.portal.pedido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository {

    void salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(UUID id);
    List<Pedido> listarTodos();
    void remover(UUID id);
}