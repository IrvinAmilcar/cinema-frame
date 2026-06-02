package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class GradeService {

    private final GradeDeExibicaoRepository gradeRepository;
    private final FilmeRepository filmeRepository;
    private final SalaRepository salaRepository;
    private final IngressoRepository ingressoRepository;

    // Construtor retrocompatível
    public GradeService(GradeDeExibicaoRepository gradeRepository,
                        FilmeRepository filmeRepository,
                        SalaRepository salaRepository) {
        this(gradeRepository, filmeRepository, salaRepository, null);
    }

    public GradeService(GradeDeExibicaoRepository gradeRepository,
                        FilmeRepository filmeRepository,
                        SalaRepository salaRepository,
                        IngressoRepository ingressoRepository) {
        if (gradeRepository == null)
            throw new IllegalArgumentException("GradeRepository não pode ser nulo");
        if (filmeRepository == null)
            throw new IllegalArgumentException("FilmeRepository não pode ser nulo");
        if (salaRepository == null)
            throw new IllegalArgumentException("SalaRepository não pode ser nulo");

        this.gradeRepository = gradeRepository;
        this.filmeRepository = filmeRepository;
        this.salaRepository = salaRepository;
        this.ingressoRepository = ingressoRepository;
    }

    public GradeDeExibicao criarGrade(LocalDate inicio, LocalDate fim) {
        GradeDeExibicao grade = new GradeDeExibicao(inicio, fim);
        gradeRepository.salvar(grade);
        return grade;
    }

    public UUID adicionarSessao(UUID gradeId, UUID filmeId, UUID salaId, LocalTime inicio) {
        GradeDeExibicao grade = gradeRepository.buscarPorId(gradeId)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + gradeId));

        Filme filme = filmeRepository.buscarPorId(filmeId)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        if (!filme.isAtivo())
            throw new IllegalStateException("Filme não está ativo e não pode ser adicionado à grade de exibição");

        Sala sala = salaRepository.buscarPorId(salaId)
            .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + salaId));

        Sessao sessao = new Sessao(filme, sala, inicio);

        // Verificação de conflito entre grades com períodos sobrepostos (regra de negócio do domínio)
        gradeRepository.listarTodas().stream()
            .filter(g -> !g.getId().equals(gradeId))
            .filter(g -> !grade.getInicio().isAfter(g.getFim()) && !g.getInicio().isAfter(grade.getFim()))
            .flatMap(g -> g.getSessoes().stream())
            .filter(s -> s.getSala().getId().equals(salaId))
            .filter(sessao::conflitaCom)
            .findFirst()
            .ifPresent(c -> { throw new IllegalStateException(
                "Conflito de horário entre grades: sala " + sala.getNumero()
                + " já está ocupada no período solicitado por outra grade"); });

        grade.adicionarSessao(sessao);
        gradeRepository.salvar(grade);
        return sessao.getId();
    }

    public GradeDeExibicao buscarPorId(UUID id) {
        return gradeRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + id));
    }

    public GradeDeExibicao buscarPorData(LocalDate data) {
        return gradeRepository.buscarPorData(data)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada para a data: " + data));
    }

    // L2: retorna a sessão removida e os ingressos que precisam de reembolso
    public ResultadoRemocaoSessao removerSessao(UUID gradeId, UUID sessaoId, LocalDateTime agora) {
        if (agora == null)
            throw new IllegalArgumentException("Horário atual não pode ser nulo");

        GradeDeExibicao grade = gradeRepository.buscarPorId(gradeId)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + gradeId));

        Sessao sessao = grade.removerSessao(sessaoId, agora);
        gradeRepository.salvar(grade);

        List<Ingresso> ingressosParaReembolso = ingressoRepository != null
            ? ingressoRepository.buscarPorSessao(sessao)
            : List.of();

        return new ResultadoRemocaoSessao(sessao, ingressosParaReembolso);
    }

    public void removerGrade(UUID id) {
        GradeDeExibicao grade = gradeRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + id));

        LocalDate hoje = LocalDate.now();
        if (!hoje.isBefore(grade.getInicio()) && !hoje.isAfter(grade.getFim())) {
            LocalTime agora = LocalTime.now();
            boolean algumaSessaoJaIniciou = grade.getSessoes().stream()
                .anyMatch(s -> !s.getInicio().isAfter(agora));
            if (algumaSessaoJaIniciou)
                throw new IllegalStateException(
                    "Não é possível remover a grade: uma ou mais sessões de hoje já foram iniciadas"
                );
        }

        gradeRepository.remover(id);
    }

    public void atualizarSessao(UUID gradeId, UUID sessaoId, UUID filmeId, UUID salaId, LocalTime novoInicio) {
        GradeDeExibicao grade = gradeRepository.buscarPorId(gradeId)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + gradeId));

        Filme novoFilme = filmeId != null
            ? filmeRepository.buscarPorId(filmeId)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId))
            : null;

        Sala novaSala = salaId != null
            ? salaRepository.buscarPorId(salaId)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + salaId))
            : null;

        grade.atualizarSessao(sessaoId, novoFilme, novaSala, novoInicio);

        Sessao sessaoAtualizada = grade.getSessoes().stream()
            .filter(s -> s.getId().equals(sessaoId))
            .findFirst().orElseThrow();

        gradeRepository.listarTodas().stream()
            .filter(g -> !g.getId().equals(gradeId))
            .filter(g -> !grade.getInicio().isAfter(g.getFim()) && !g.getInicio().isAfter(grade.getFim()))
            .flatMap(g -> g.getSessoes().stream())
            .filter(s -> s.getSala().getId().equals(sessaoAtualizada.getSala().getId()))
            .filter(sessaoAtualizada::conflitaCom)
            .findFirst()
            .ifPresent(c -> { throw new IllegalStateException(
                "Conflito de horário entre grades: sala " + sessaoAtualizada.getSala().getNumero()
                + " já está ocupada no período solicitado por outra grade"); });

        gradeRepository.salvar(grade);
    }

    public GradeDeExibicao atualizarGrade(UUID id, LocalDate novoInicio, LocalDate novoFim) {
        GradeDeExibicao grade = gradeRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + id));
        grade.atualizarPeriodo(novoInicio, novoFim);
        gradeRepository.salvar(grade);
        return grade;
    }

    public List<GradeDeExibicao> listarTodas() {
        return gradeRepository.listarTodas();
    }
}
