package br.com.cinema.frame.infrastructure.pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CupomJpaRepository extends JpaRepository<CupomJpa, UUID> {
    Optional<CupomJpa> findByCodigo(String codigo);
}
