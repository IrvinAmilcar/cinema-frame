package br.com.cinema.frame.domain.portal.programacao;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProgramacaoService {

    private final GradeDeExibicaoRepository gradeRepository;

    public ProgramacaoService(GradeDeExibicaoRepository gradeRepository) {
        if (gradeRepository == null)
            throw new IllegalArgumentException("GradeDeExibicaoRepository não pode ser nulo");
        this.gradeRepository = gradeRepository;
    }

    public List<Sessao> listarSessoesDisponiveis(LocalDateTime agora) {
        if (agora == null)
            throw new IllegalArgumentException("Horário atual não pode ser nulo");

        return gradeRepository.listarTodas().stream()
            .flatMap(g -> g.getSessoes().stream())
            .filter(s -> s.getInicio().isAfter(agora))
            .filter(s -> s.getFilme().isAtivo())
            .collect(Collectors.toList());
    }

    public List<Sessao> listarSessoesDisponiveisPorIdade(LocalDateTime agora, int idadeCliente) {
        if (agora == null)
            throw new IllegalArgumentException("Horário atual não pode ser nulo");
        if (idadeCliente < 0)
            throw new IllegalArgumentException("Idade do cliente não pode ser negativa");

        return listarSessoesDisponiveis(agora).stream()
            .filter(s -> idadeCliente >= s.getFilme().getClassificacaoIndicativa().getIdadeMinima())
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
}