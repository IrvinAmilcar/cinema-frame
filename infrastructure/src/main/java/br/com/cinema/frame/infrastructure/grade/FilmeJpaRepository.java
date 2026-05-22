package br.com.cinema.frame.infrastructure.grade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FilmeJpaRepository extends JpaRepository<FilmeJpa, UUID> {
    List<FilmeJpa> findByAtivo(boolean ativo);
}
