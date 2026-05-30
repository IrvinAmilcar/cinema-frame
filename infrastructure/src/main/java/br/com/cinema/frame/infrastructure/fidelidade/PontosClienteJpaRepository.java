package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PontosClienteJpaRepository extends JpaRepository<PontosClienteJpa, UUID> {
}
