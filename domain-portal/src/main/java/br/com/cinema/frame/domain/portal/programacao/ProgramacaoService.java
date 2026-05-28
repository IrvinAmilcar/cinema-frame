package br.com.cinema.frame.domain.portal.programacao;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProgramacaoService {

    private final GradeDeExibicaoRepository gradeRepository;
    private final IngressoRepository ingressoRepository;

    public ProgramacaoService(GradeDeExibicaoRepository gradeRepository) {
        this(gradeRepository, null);
    }

    public ProgramacaoService(GradeDeExibicaoRepository gradeRepository,
                               IngressoRepository ingressoRepository) {
        if (gradeRepository == null)
            throw new IllegalArgumentException("GradeDeExibicaoRepository não pode ser nulo");
        this.gradeRepository = gradeRepository;
        this.ingressoRepository = ingressoRepository;
    }

    public List<Sessao> listarSessoesDisponiveis(LocalDateTime agora) {
        if (agora == null)
            throw new IllegalArgumentException("Horário atual não pode ser nulo");

        var hoje = agora.toLocalDate();
        var horaAtual = agora.toLocalTime();

        return gradeRepository.listarTodas().stream()
            .filter(g -> !g.getInicio().isAfter(hoje) && !g.getFim().isBefore(hoje))
            .flatMap(g -> g.getSessoes().stream())
            .filter(s -> s.getInicio().isAfter(horaAtual))
            .collect(Collectors.toList());
    }

    public List<Sessao> filtrarPorGenero(LocalDateTime agora, GeneroFilme genero) {
        if (genero == null)
            throw new IllegalArgumentException("Gênero não pode ser nulo");

        return listarSessoesDisponiveis(agora).stream()
            .filter(s -> s.getFilme().getGenero() == genero)
            .collect(Collectors.toList());
    }

    public List<Sessao> filtrarPorClassificacao(LocalDateTime agora, ClassificacaoIndicativa classificacaoMaxima) {
        if (classificacaoMaxima == null)
            throw new IllegalArgumentException("Classificação não pode ser nula");

        return listarSessoesDisponiveis(agora).stream()
            .filter(s -> s.getFilme().getClassificacaoIndicativa().getIdadeMinima()
                <= classificacaoMaxima.getIdadeMinima())
            .collect(Collectors.toList());
    }

    public List<Sessao> listarSessoesPorData(LocalDate data) {
        if (data == null)
            throw new IllegalArgumentException("Data não pode ser nula");

        LocalDate hoje = LocalDate.now();
        LocalTime horaAtual = LocalTime.now();

        return gradeRepository.listarTodas().stream()
            .filter(g -> !g.getInicio().isAfter(data) && !g.getFim().isBefore(data))
            .flatMap(g -> g.getSessoes().stream())
            .filter(s -> data.isAfter(hoje) || s.getInicio().isAfter(horaAtual))
            .collect(Collectors.toList());
    }

    public List<Sessao> ordenarPorPopularidade(LocalDateTime agora, Map<UUID, Integer> ingressosPorSessao) {
        if (agora == null)
            throw new IllegalArgumentException("Horário atual não pode ser nulo");
        if (ingressosPorSessao == null)
            throw new IllegalArgumentException("Mapa de ingressos não pode ser nulo");

        return listarSessoesDisponiveis(agora).stream()
            .sorted(Comparator.comparingInt(
                (Sessao s) -> ingressosPorSessao.getOrDefault(s.getId(), 0)
            ).reversed())
            .collect(Collectors.toList());
    }

    public List<Sessao> ordenarPorPopularidade(LocalDateTime agora) {
        if (agora == null)
            throw new IllegalArgumentException("Horário atual não pode ser nulo");

        return ordenarPorPopularidade(agora, listarSessoesDisponiveis(agora));
    }

    public List<Sessao> ordenarPorPopularidade(LocalDateTime agora, List<Sessao> sessoes) {
        if (agora == null)
            throw new IllegalArgumentException("Horário atual não pode ser nulo");
        if (sessoes == null || sessoes.isEmpty()) return sessoes;
        if (ingressoRepository == null) return sessoes;

        return sessoes.stream()
            .sorted(Comparator.comparingInt(
                (Sessao s) -> ingressoRepository.buscarPorSessao(s).size()
            ).reversed())
            .collect(Collectors.toList());
    }
}