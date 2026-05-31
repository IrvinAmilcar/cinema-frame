package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResgateJpaRepository extends JpaRepository<ResgateJpa, UUID> {

    List<ResgateJpa> findByClienteId(UUID clienteId);

    // FUNCTION('MONTH',...) não funciona no PostgreSQL — usar EXTRACT
    @Query("SELECT r FROM ResgateJpa r WHERE r.clienteId = :clienteId " +
           "AND EXTRACT(MONTH FROM r.dataHora) = :mes " +
           "AND EXTRACT(YEAR FROM r.dataHora) = :ano")
    List<ResgateJpa> findByClienteIdAndMesAno(
            @Param("clienteId") UUID clienteId,
            @Param("mes") int mes,
            @Param("ano") int ano);
}
