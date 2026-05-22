package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.infrastructure.sala.SalaJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GradeDeExibicaoRepositoryAdapter implements GradeDeExibicaoRepository {

    private final GradeJpaRepository gradeJpa;
    private final SessaoJpaRepository sessaoJpa;
    private final FilmeJpaRepository filmeJpa;
    private final SalaJpaRepository salaJpa;

    public GradeDeExibicaoRepositoryAdapter(GradeJpaRepository gradeJpa,
                                            SessaoJpaRepository sessaoJpa,
                                            FilmeJpaRepository filmeJpa,
                                            SalaJpaRepository salaJpa) {
        this.gradeJpa = gradeJpa;
        this.sessaoJpa = sessaoJpa;
        this.filmeJpa = filmeJpa;
        this.salaJpa = salaJpa;
    }

    @Override
    @Transactional
    public void salvar(GradeDeExibicao grade) {
        gradeJpa.save(GradeJpa.fromDomain(grade));
        sessaoJpa.deleteByGradeId(grade.getId());
        for (Sessao s : grade.getSessoes()) {
            sessaoJpa.save(SessaoJpa.fromDomain(grade.getId(), s));
        }
    }

    @Override
    public Optional<GradeDeExibicao> buscarPorId(UUID id) {
        return gradeJpa.findById(id).map(g -> reconstituir(g, sessaoJpa.findByGradeId(g.getId())));
    }

    @Override
    public Optional<GradeDeExibicao> buscarPorData(LocalDate data) {
        return gradeJpa.findByData(data).map(g -> reconstituir(g, sessaoJpa.findByGradeId(g.getId())));
    }

    @Override
    public List<GradeDeExibicao> listarTodas() {
        return gradeJpa.findAll().stream()
            .map(g -> reconstituir(g, sessaoJpa.findByGradeId(g.getId())))
            .toList();
    }

    @Override
    @Transactional
    public void remover(UUID id) {
        sessaoJpa.deleteByGradeId(id);
        gradeJpa.deleteById(id);
    }

    private GradeDeExibicao reconstituir(GradeJpa g, List<SessaoJpa> sessoes) {
        List<Sessao> domainSessoes = sessoes.stream().map(this::reconstituirSessao).toList();
        return GradeDeExibicao.reconstituir(g.getId(), g.getInicio(), g.getFim(), domainSessoes);
    }

    private Sessao reconstituirSessao(SessaoJpa jpa) {
        var filme = filmeJpa.findById(jpa.getFilmeId())
            .orElseThrow(() -> new IllegalStateException("Filme não encontrado: " + jpa.getFilmeId()))
            .toDomain();
        var sala = salaJpa.findById(jpa.getSalaId())
            .orElseThrow(() -> new IllegalStateException("Sala não encontrada: " + jpa.getSalaId()))
            .toDomain();
        return jpa.toDomain(filme, sala);
    }
}
