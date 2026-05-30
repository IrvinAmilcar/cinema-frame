package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficioJpaRepository extends JpaRepository<BeneficioJpa, UUID> {
}
