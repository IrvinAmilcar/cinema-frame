package br.com.cinema.frame.infrastructure.pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpa, UUID> {
    List<PedidoJpa> findByClienteIdAndFinalizadoTrue(UUID clienteId);

    @Query("SELECT p FROM PedidoJpa p WHERE p.clienteId = :clienteId AND p.finalizado = true AND (p.dataSessao IS NULL OR p.dataSessao >= :data)")
    List<PedidoJpa> findAtivosDoCliente(@Param("clienteId") UUID clienteId, @Param("data") LocalDate data);
}
