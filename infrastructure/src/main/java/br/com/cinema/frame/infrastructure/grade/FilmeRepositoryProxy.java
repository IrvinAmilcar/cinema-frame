package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Proxy (F7) — Caching Proxy sobre FilmeRepository.
 *
 * Controla o acesso ao repositório real evitando consultas repetidas ao banco:
 * listarTodos() retorna o cache em memória; qualquer escrita invalida o cache.
 * Regras de negócio (proteção contra remoção de filme com sessões futuras)
 * residem no FilmeService.
 */
@Repository
@Primary
public class FilmeRepositoryProxy implements FilmeRepository {

    private final FilmeRepositoryAdapter real;
    private List<Filme> cache = null;

    public FilmeRepositoryProxy(FilmeRepositoryAdapter real) {
        this.real = real;
    }

    @Override
    public List<Filme> listarTodos() {
        if (cache == null) {
            cache = real.listarTodos();
        }
        return cache;
    }

    @Override
    public void salvar(Filme filme) {
        cache = null;
        real.salvar(filme);
    }

    @Override
    public void remover(UUID id) {
        cache = null;
        real.remover(id);
    }

    @Override
    public Optional<Filme> buscarPorId(UUID id) {
        return real.buscarPorId(id);
    }
}
