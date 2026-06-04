package br.com.cinema.frame.infrastructure.pedido;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoProdutoJpaRepository extends JpaRepository<PedidoProdutoJpa, UUID> {

    @Query("SELECT COALESCE(SUM(p.preco), 0) FROM PedidoProdutoJpa p WHERE p.dataPedido BETWEEN :dataInicio AND :dataFim")
    double somarVendasBombonierePorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    void deleteByPedidoId(UUID pedidoId);
}
