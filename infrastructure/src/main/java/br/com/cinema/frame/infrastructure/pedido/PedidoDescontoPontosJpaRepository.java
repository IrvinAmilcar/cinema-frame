package br.com.cinema.frame.infrastructure.pedido;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoDescontoPontosJpaRepository extends JpaRepository<PedidoDescontoPontosJpa, UUID> {

    @Query("SELECT COALESCE(SUM(d.valorDesconto), 0) FROM PedidoDescontoPontosJpa d WHERE d.dataPedido BETWEEN :dataInicio AND :dataFim")
    double somarDescontosPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}
