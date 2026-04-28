package br.com.cinema.frame.domain.portal.programacao;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.time.LocalDateTime;
import java.util.List;
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
}