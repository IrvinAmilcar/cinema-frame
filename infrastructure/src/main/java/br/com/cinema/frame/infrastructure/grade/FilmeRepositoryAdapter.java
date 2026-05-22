package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FilmeRepositoryAdapter implements FilmeRepository {

    private final FilmeJpaRepository jpa;

    public FilmeRepositoryAdapter(FilmeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Filme filme) {
        jpa.save(FilmeJpa.fromDomain(filme));
    }

    @Override
    public Optional<Filme> buscarPorId(UUID id) {
        return jpa.findById(id).map(FilmeJpa::toDomain);
    }

    @Override
    public List<Filme> listarTodos() {
        return jpa.findAll().stream().map(FilmeJpa::toDomain).toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
    }
}
