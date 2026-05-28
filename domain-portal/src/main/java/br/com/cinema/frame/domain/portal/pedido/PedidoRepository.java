package br.com.cinema.frame.domain.portal.pedido;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository {

    void salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(UUID id);
    List<Pedido> listarTodos();
    List<Pedido> buscarFinalizadosPorCliente(UUID clienteId);
    List<Pedido> buscarFinalizadosPorClienteAPartirDe(UUID clienteId, LocalDate dataMinima);
    void remover(UUID id);
}