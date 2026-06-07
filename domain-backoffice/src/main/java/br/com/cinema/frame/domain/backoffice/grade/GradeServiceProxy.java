package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Proxy (F4) — Protection Proxy para GradeServiceInterface.
 *
 * Controla o acesso a adicionarSessao() e atualizarSessao(): intercepta as chamadas
 * e verifica se há conflito de horário com sessões de OUTRAS grades cujos períodos
 * se sobrepõem. A verificação de conflito dentro da própria grade fica em GradeDeExibicao.
 * Todas as outras operações são delegadas diretamente sem interceptação.
 */
public class GradeServiceProxy implements GradeServiceInterface {

    private final GradeServiceInterface real;
    private final GradeDeExibicaoRepository gradeRepository;
    private final FilmeRepository filmeRepository;
    private final SalaRepository salaRepository;

    public GradeServiceProxy(GradeServiceInterface real,
                              GradeDeExibicaoRepository gradeRepository,
                              FilmeRepository filmeRepository,
                              SalaRepository salaRepository) {
        if (real == null) throw new IllegalArgumentException("GradeServiceInterface não pode ser nulo");
        this.real = real;
        this.gradeRepository = gradeRepository;
        this.filmeRepository = filmeRepository;
        this.salaRepository = salaRepository;
    }

    @Override
    public UUID adicionarSessao(UUID gradeId, UUID filmeId, UUID salaId, LocalTime inicio) {
        GradeDeExibicao grade = real.buscarPorId(gradeId);
        Filme filme = filmeRepository.buscarPorId(filmeId)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));
        Sala sala = salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + salaId));

        Sessao novaSessao = new Sessao(filme, sala, inicio);

        gradeRepository.listarTodas().stream()
                .filter(g -> !g.getId().equals(gradeId))
                .filter(g -> !grade.getInicio().isAfter(g.getFim()) && !g.getInicio().isAfter(grade.getFim()))
                .flatMap(g -> g.getSessoes().stream())
                .filter(s -> s.getSala().getId().equals(salaId))
                .filter(novaSessao::conflitaCom)
                .findFirst()
                .ifPresent(c -> { throw new IllegalStateException(
                        "Conflito de horário entre grades: sala " + sala.getNumero()
                        + " já está ocupada no período solicitado por outra grade"); });

        return real.adicionarSessao(gradeId, filmeId, salaId, inicio);
    }

    @Override
    public void atualizarSessao(UUID gradeId, UUID sessaoId, UUID filmeId, UUID salaId, LocalTime novoInicio) {
        GradeDeExibicao grade = real.buscarPorId(gradeId);

        Sessao sessaoAtual = grade.getSessoes().stream()
                .filter(s -> s.getId().equals(sessaoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + sessaoId));

        Filme filmeEfetivo = filmeId != null
                ? filmeRepository.buscarPorId(filmeId).orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId))
                : sessaoAtual.getFilme();
        Sala salaEfetiva = salaId != null
                ? salaRepository.buscarPorId(salaId).orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + salaId))
                : sessaoAtual.getSala();
        LocalTime inicioEfetivo = novoInicio != null ? novoInicio : sessaoAtual.getInicio();

        Sessao sessaoAtualizada = new Sessao(filmeEfetivo, salaEfetiva, inicioEfetivo);

        gradeRepository.listarTodas().stream()
                .filter(g -> !g.getId().equals(gradeId))
                .filter(g -> !grade.getInicio().isAfter(g.getFim()) && !g.getInicio().isAfter(grade.getFim()))
                .flatMap(g -> g.getSessoes().stream())
                .filter(s -> s.getSala().getId().equals(salaEfetiva.getId()))
                .filter(sessaoAtualizada::conflitaCom)
                .findFirst()
                .ifPresent(c -> { throw new IllegalStateException(
                        "Conflito de horário entre grades: sala " + salaEfetiva.getNumero()
                        + " já está ocupada no período solicitado por outra grade"); });

        real.atualizarSessao(gradeId, sessaoId, filmeId, salaId, novoInicio);
    }

    @Override public GradeDeExibicao criarGrade(LocalDate inicio, LocalDate fim)      { return real.criarGrade(inicio, fim); }
    @Override public GradeDeExibicao buscarPorId(UUID id)                             { return real.buscarPorId(id); }
    @Override public GradeDeExibicao buscarPorData(LocalDate data)                    { return real.buscarPorData(data); }
    @Override public void removerGrade(UUID id)                                        { real.removerGrade(id); }
    @Override public GradeDeExibicao atualizarGrade(UUID id, LocalDate i, LocalDate f){ return real.atualizarGrade(id, i, f); }
    @Override public List<GradeDeExibicao> listarTodas()                              { return real.listarTodas(); }

    @Override
    public ResultadoRemocaoSessao removerSessao(UUID gradeId, UUID sessaoId, LocalDateTime agora) {
        return real.removerSessao(gradeId, sessaoId, agora);
    }
}
