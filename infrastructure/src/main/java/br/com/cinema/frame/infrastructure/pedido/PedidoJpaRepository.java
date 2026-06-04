package br.com.cinema.frame.infrastructure.pedido;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpa, UUID> {
    List<PedidoJpa> findByClienteIdAndFinalizadoTrue(UUID clienteId);

    @Query("SELECT p FROM PedidoJpa p WHERE p.clienteId = :clienteId AND p.finalizado = true AND (p.dataSessao IS NULL OR p.dataSessao >= :data)")
    List<PedidoJpa> findAtivosDoCliente(@Param("clienteId") UUID clienteId, @Param("data") LocalDate data);
      boolean existsBySessaoId(UUID sessaoId);

    @Query("SELECT COUNT(i) FROM IngressoJpa i WHERE i.pedidoId IN (SELECT p.id FROM PedidoJpa p WHERE p.finalizado = true AND p.dataSessao BETWEEN :dataInicio AND :dataFim)")
    int contarIngressosPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Modifying
    @Transactional
    @Query("UPDATE PedidoJpa p SET p.dataSessao = CURRENT_DATE WHERE p.dataSessao IS NULL AND p.finalizado = true")
    int preencherDataSessaoNula();

    @Query("SELECT p FROM PedidoJpa p WHERE p.finalizado = true AND p.dataSessao BETWEEN :dataInicio AND :dataFim")
    List<PedidoJpa> findFinalizadosByPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query(value = """
        SELECT COALESCE(SUM(
            20.0
            * (1 + CASE s.tipo
                WHEN 'PADRAO'  THEN 0.0
                WHEN 'TRES_D'  THEN 0.5
                WHEN 'IMAX'    THEN 1.2
                WHEN 'VIP'     THEN 2.0
                ELSE 0.0 END)
            * CASE i.tipo
                WHEN 'INTEIRA'  THEN 1.0
                WHEN 'MEIA'     THEN 0.5
                WHEN 'CONVITE'  THEN 0.0
                ELSE 1.0 END
        ), 0)
        FROM ingressos i
        JOIN pedidos p ON i.pedido_id = p.id
        JOIN sessoes se ON p.sessao_id = se.id
        JOIN salas s ON se.sala_id = s.id
        WHERE p.finalizado = true
          AND p.data_sessao BETWEEN :dataInicio AND :dataFim
        """, nativeQuery = true)
    double somarValorIngressosPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}