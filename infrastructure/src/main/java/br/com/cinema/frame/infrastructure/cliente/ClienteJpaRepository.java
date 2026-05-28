package br.com.cinema.frame.infrastructure.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpa, UUID> {
    Optional<ClienteJpa> findByEmail(String email);
}
