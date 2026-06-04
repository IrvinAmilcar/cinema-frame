package br.com.cinema.frame.infrastructure.pedido;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoResumoFinanceiroJpaRepository extends JpaRepository<PedidoResumoFinanceiroJpa, UUID> {

    @Query("SELECT COALESCE(SUM(r.valorIngressos), 0) FROM PedidoResumoFinanceiroJpa r WHERE r.dataPedido BETWEEN :dataInicio AND :dataFim")
    double somarValorIngressosPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COALESCE(SUM(r.valorBomboniere), 0) FROM PedidoResumoFinanceiroJpa r WHERE r.dataPedido BETWEEN :dataInicio AND :dataFim")
    double somarValorBombonierePorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COALESCE(SUM(r.descontoPontos), 0) FROM PedidoResumoFinanceiroJpa r WHERE r.dataPedido BETWEEN :dataInicio AND :dataFim")
    double somarDescontoPontosPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COALESCE(SUM(r.valorTotal), 0) FROM PedidoResumoFinanceiroJpa r WHERE r.dataPedido BETWEEN :dataInicio AND :dataFim")
    double somarReceitaTotalPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}
