package br.com.cinema.frame.infrastructure.bomboniere;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoJpaRepository
        extends JpaRepository<ProdutoJpa, UUID> {

    List<ProdutoJpa> findByAtivo(boolean ativo);

    Optional<ProdutoJpa> findByNome(String nome);

}