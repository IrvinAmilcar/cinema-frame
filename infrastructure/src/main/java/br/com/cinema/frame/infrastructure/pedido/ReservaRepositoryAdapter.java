package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.portal.reserva.ReservaDeAssento;
import br.com.cinema.frame.domain.portal.reserva.ReservaRepository;
import br.com.cinema.frame.infrastructure.grade.FilmeJpaRepository;
import br.com.cinema.frame.infrastructure.grade.GradeJpaRepository;
import br.com.cinema.frame.infrastructure.grade.SessaoJpaRepository;
import br.com.cinema.frame.infrastructure.sala.SalaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReservaRepositoryAdapter implements ReservaRepository {

    private final ReservaJpaRepository jpa;
    private final SessaoJpaRepository sessaoJpa;
    private final FilmeJpaRepository filmeJpa;
    private final SalaJpaRepository salaJpa;
    private final GradeJpaRepository gradeJpa;

    public ReservaRepositoryAdapter(ReservaJpaRepository jpa,
                                    SessaoJpaRepository sessaoJpa,
                                    FilmeJpaRepository filmeJpa,
                                    SalaJpaRepository salaJpa,
                                    GradeJpaRepository gradeJpa) {
        this.jpa = jpa;
        this.sessaoJpa = sessaoJpa;
        this.filmeJpa = filmeJpa;
        this.salaJpa = salaJpa;
        this.gradeJpa = gradeJpa;
    }

    @Override
    public void salvar(ReservaDeAssento reserva) {
        jpa.save(ReservaJpa.fromDomain(reserva));
    }

    @Override
    public Optional<ReservaDeAssento> buscarPorId(UUID id) {
        return jpa.findById(id).map(r -> r.toDomain(buscarSessao(r.getSessaoId())));
    }

    @Override
    public List<ReservaDeAssento> buscarPorSessaoId(UUID sessaoId) {
        Sessao sessao = buscarSessao(sessaoId);
        return jpa.findBySessaoId(sessaoId).stream()
                .map(r -> r.toDomain(sessao))
                .toList();
    }

    @Override
    public void remover(UUID id) {
        jpa.deleteById(id);
    }

    private Sessao buscarSessao(UUID sessaoId) {
        var sessaoJpaOpt = sessaoJpa.findById(sessaoId)
                .orElseThrow(() -> new IllegalStateException("Sessão não encontrada: " + sessaoId));
        var filme = filmeJpa.findById(sessaoJpaOpt.getFilmeId())
                .orElseThrow(() -> new IllegalStateException("Filme não encontrado"))
                .toDomain();
        var sala = salaJpa.findById(sessaoJpaOpt.getSalaId())
                .orElseThrow(() -> new IllegalStateException("Sala não encontrada"))
                .toDomain();
        return sessaoJpaOpt.toDomain(filme, sala);
    }
}
