package br.com.cinema.frame.infrastructure.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpa, UUID> {
}
