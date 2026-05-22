package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.infrastructure.sala.SalaJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Proxy (F4): intercepta salvar() e bloqueia se houver conflito de horário
 * entre sessões de grades diferentes na mesma sala.
 * O domínio já verifica conflitos DENTRO da mesma grade; este proxy estende
 * a verificação ENTRE grades distintas.
 */
@Repository
@Primary
public class GradeDeExibicaoRepositoryProxy implements GradeDeExibicaoRepository {

    private final GradeDeExibicaoRepositoryAdapter real;
    private final SessaoJpaRepository sessaoJpa;
    private final FilmeJpaRepository filmeJpa;
    private final SalaJpaRepository salaJpa;

    public GradeDeExibicaoRepositoryProxy(GradeDeExibicaoRepositoryAdapter real,
                                          SessaoJpaRepository sessaoJpa,
                                          FilmeJpaRepository filmeJpa,
                                          SalaJpaRepository salaJpa) {
        this.real = real;
        this.sessaoJpa = sessaoJpa;
        this.filmeJpa = filmeJpa;
        this.salaJpa = salaJpa;
    }

    @Override
    public void salvar(GradeDeExibicao grade) {
        for (Sessao novaSessao : grade.getSessoes()) {
            List<SessaoJpa> candidatas = sessaoJpa.findBySalaId(novaSessao.getSala().getId())
                .stream()
                .filter(s -> !s.getGradeId().equals(grade.getId()))
                .toList();

            for (SessaoJpa candidataJpa : candidatas) {
                var filme = filmeJpa.findById(candidataJpa.getFilmeId())
                    .orElseThrow(() -> new IllegalStateException("Filme não encontrado: " + candidataJpa.getFilmeId()))
                    .toDomain();
                var sala = salaJpa.findById(candidataJpa.getSalaId())
                    .orElseThrow(() -> new IllegalStateException("Sala não encontrada: " + candidataJpa.getSalaId()))
                    .toDomain();
                Sessao sessaoExistente = candidataJpa.toDomain(filme, sala);

                if (sessaoExistente.conflitaCom(novaSessao))
                    throw new IllegalStateException(
                        "Conflito de horário entre grades: sala " + novaSessao.getSala().getNumero()
                        + " já está ocupada no período solicitado"
                    );
            }
        }
        real.salvar(grade);
    }

    @Override
    public Optional<GradeDeExibicao> buscarPorId(UUID id) { return real.buscarPorId(id); }

    @Override
    public Optional<GradeDeExibicao> buscarPorData(LocalDate data) { return real.buscarPorData(data); }

    @Override
    public List<GradeDeExibicao> listarTodas() { return real.listarTodas(); }

    @Override
    public void remover(UUID id) { real.remover(id); }
}
