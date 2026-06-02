package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Proxy (F4) — Caching Proxy sobre GradeDeExibicaoRepository.
 *
 * Controla o acesso ao repositório real evitando consultas repetidas ao banco:
 * listarTodas() retorna o cache em memória; qualquer escrita invalida o cache.
 * Regras de negócio (conflito de horário entre grades) residem no GradeService.
 */
@Repository
@Primary
public class GradeDeExibicaoRepositoryProxy implements GradeDeExibicaoRepository {

    private final GradeDeExibicaoRepositoryAdapter real;
    private List<GradeDeExibicao> cache = null;

    public GradeDeExibicaoRepositoryProxy(GradeDeExibicaoRepositoryAdapter real) {
        this.real = real;
    }

    @Override
    public List<GradeDeExibicao> listarTodas() {
        if (cache == null) {
            cache = real.listarTodas();
        }
        return cache;
    }

    @Override
    public void salvar(GradeDeExibicao grade) {
        cache = null;
        real.salvar(grade);
    }

    @Override
    public void remover(UUID id) {
        cache = null;
        real.remover(id);
    }

    @Override
    public Optional<GradeDeExibicao> buscarPorId(UUID id) {
        return real.buscarPorId(id);
    }

    @Override
    public Optional<GradeDeExibicao> buscarPorData(LocalDate data) {
        return real.buscarPorData(data);
    }
}
