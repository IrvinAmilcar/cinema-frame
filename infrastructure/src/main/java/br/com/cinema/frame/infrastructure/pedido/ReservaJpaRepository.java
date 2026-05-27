package br.com.cinema.frame.infrastructure.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservaJpaRepository extends JpaRepository<ReservaJpa, UUID> {
    List<ReservaJpa> findBySessaoId(UUID sessaoId);
}
