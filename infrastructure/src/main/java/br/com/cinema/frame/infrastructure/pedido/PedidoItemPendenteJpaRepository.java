package br.com.cinema.frame.infrastructure.pedido;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoItemPendenteJpaRepository extends JpaRepository<PedidoItemPendenteJpa, UUID> {
    List<PedidoItemPendenteJpa> findByPedidoId(UUID pedidoId);
    void deleteByPedidoId(UUID pedidoId);
}
