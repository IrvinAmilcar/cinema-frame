package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.grade.SessaoRepository;
import br.com.cinema.frame.infrastructure.sala.SalaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SessaoRepositoryAdapter implements SessaoRepository {

    private final SessaoJpaRepository sessaoJpa;
    private final FilmeJpaRepository filmeJpa;
    private final SalaJpaRepository salaJpa;

    public SessaoRepositoryAdapter(SessaoJpaRepository sessaoJpa,
                                   FilmeJpaRepository filmeJpa,
                                   SalaJpaRepository salaJpa) {
        this.sessaoJpa = sessaoJpa;
        this.filmeJpa = filmeJpa;
        this.salaJpa = salaJpa;
    }

    @Override
    public void salvar(Sessao sessao) {
        throw new UnsupportedOperationException("Sessões são salvas através de GradeDeExibicaoRepository");
    }

    @Override
    public Optional<Sessao> buscarPorId(UUID id) {
        return sessaoJpa.findById(id).map(this::reconstituir);
    }

    @Override
    public List<Sessao> buscarPorFilme(UUID filmeId) {
        return sessaoJpa.findByFilmeId(filmeId).stream()
            .map(this::reconstituir)
            .toList();
    }

    @Override
    public List<Sessao> listarTodas() {
        return sessaoJpa.findAll().stream()
            .map(this::reconstituir)
            .toList();
    }

    @Override
    public void remover(UUID id) {
        sessaoJpa.deleteById(id);
    }

    private Sessao reconstituir(SessaoJpa jpa) {
        var filme = filmeJpa.findById(jpa.getFilmeId())
            .orElseThrow(() -> new IllegalStateException("Filme não encontrado: " + jpa.getFilmeId()))
            .toDomain();
        var sala = salaJpa.findById(jpa.getSalaId())
            .orElseThrow(() -> new IllegalStateException("Sala não encontrada: " + jpa.getSalaId()))
            .toDomain();
        return jpa.toDomain(filme, sala);
    }
}
