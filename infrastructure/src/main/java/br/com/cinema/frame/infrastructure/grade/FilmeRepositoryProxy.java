package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.SessaoRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Proxy (F7): intercepta remover() e bloqueia se o filme possui sessões futuras.
 * "Futuras" = pertencentes a grades ainda ativas com horário não expirado hoje.
 */
@Repository
@Primary
public class FilmeRepositoryProxy implements FilmeRepository {

    private final FilmeRepositoryAdapter real;
    private final SessaoRepository sessaoRepository;

    public FilmeRepositoryProxy(FilmeRepositoryAdapter real, SessaoRepository sessaoRepository) {
        this.real = real;
        this.sessaoRepository = sessaoRepository;
    }

    @Override
    public void remover(UUID id) {
        boolean temSessoesFuturas = !sessaoRepository.buscarPorFilme(id).isEmpty();

        if (temSessoesFuturas)
            throw new IllegalStateException("Filme possui sessões futuras e não pode ser removido");

        real.remover(id);
    }

    @Override
    public void salvar(Filme filme) { real.salvar(filme); }

    @Override
    public Optional<Filme> buscarPorId(UUID id) { return real.buscarPorId(id); }

    @Override
    public List<Filme> listarTodos() { return real.listarTodos(); }
}
