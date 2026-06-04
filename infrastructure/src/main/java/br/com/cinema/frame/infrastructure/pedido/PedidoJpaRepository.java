package br.com.cinema.frame.infrastructure.pedido;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpa, UUID> {
    List<PedidoJpa> findByClienteIdAndFinalizadoTrue(UUID clienteId);

    @Query("SELECT p FROM PedidoJpa p WHERE p.clienteId = :clienteId AND p.finalizado = true AND (p.dataSessao IS NULL OR p.dataSessao >= :data)")
    List<PedidoJpa> findAtivosDoCliente(@Param("clienteId") UUID clienteId, @Param("data") LocalDate data);
      boolean existsBySessaoId(UUID sessaoId);

    @Query("SELECT COUNT(i) FROM IngressoJpa i WHERE i.pedidoId IN (SELECT p.id FROM PedidoJpa p WHERE p.finalizado = true AND p.dataSessao IS NOT NULL AND p.dataSessao BETWEEN :dataInicio AND :dataFim)")
    int contarIngressosPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT p FROM PedidoJpa p WHERE p.finalizado = true AND p.dataSessao BETWEEN :dataInicio AND :dataFim")
    List<PedidoJpa> findFinalizadosByPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}