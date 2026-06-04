package br.com.cinema.frame.infrastructure.caixa;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FechamentoCaixaJpaRepository extends JpaRepository<FechamentoCaixaEntity, UUID> {

    Optional<FechamentoCaixaEntity> findByData(LocalDate data);

    boolean existsByData(LocalDate data);
}