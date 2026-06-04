package br.com.cinema.frame.infrastructure.pedido;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoProdutoJpaRepository extends JpaRepository<PedidoProdutoJpa, UUID> {

    @Query("SELECT COALESCE(SUM(p.preco), 0) FROM PedidoProdutoJpa p WHERE p.dataPedido BETWEEN :dataInicio AND :dataFim")
    double somarVendasBombonierePorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    void deleteByPedidoId(UUID pedidoId);

    @Query(value = """
        SELECT
            se.id           AS sessao_id,
            f.titulo        AS filme,
            se.inicio       AS horario,
            s.numero        AS sala_numero,
            s.tipo          AS sala_tipo,
            pp.nome_produto AS produto,
            COUNT(pp.id)    AS quantidade,
            SUM(pp.preco)   AS valor_total
        FROM pedido_produtos pp
        JOIN pedidos p  ON pp.pedido_id = p.id
        JOIN sessoes se ON p.sessao_id  = se.id
        JOIN grades  g  ON se.grade_id  = g.id
        JOIN filmes  f  ON se.filme_id  = f.id
        JOIN salas   s  ON se.sala_id   = s.id
        WHERE pp.data_pedido BETWEEN :dataInicio AND :dataFim
          AND pp.data_pedido BETWEEN g.inicio AND g.fim
        GROUP BY se.id, f.titulo, se.inicio, s.numero, s.tipo, pp.nome_produto
        ORDER BY se.inicio, f.titulo, pp.nome_produto
        """, nativeQuery = true)
    List<Object[]> bombonierePorSessao(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}