package br.com.cinema.frame.infrastructure.sala;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SalaJpaRepository extends JpaRepository<SalaJpa, UUID> {
    Optional<SalaJpa> findByNumero(int numero);
}
