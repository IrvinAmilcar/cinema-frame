package br.com.cinema.frame.infrastructure.bomboniere;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InsumoJpaRepository
        extends JpaRepository<InsumoJpa, UUID> {

    Optional<InsumoJpa> findByNome(String nome);

    List<InsumoJpa> findByQuantidadeEmEstoqueLessThanEqual(
            double nivelCritico
    );
}