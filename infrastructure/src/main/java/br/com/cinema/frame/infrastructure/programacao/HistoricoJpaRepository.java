package br.com.cinema.frame.infrastructure.programacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HistoricoJpaRepository extends JpaRepository<HistoricoJpa, UUID> {
    Optional<HistoricoJpa> findByClienteId(UUID clienteId);
}
