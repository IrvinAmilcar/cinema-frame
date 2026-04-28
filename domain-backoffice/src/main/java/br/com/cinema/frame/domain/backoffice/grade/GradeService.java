package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.backoffice.sala.Sala;
import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class GradeService {

    private final GradeDeExibicaoRepository gradeRepository;
    private final FilmeRepository filmeRepository;
    private final SalaRepository salaRepository;

    public GradeService(GradeDeExibicaoRepository gradeRepository, FilmeRepository filmeRepository, SalaRepository salaRepository) {
        if (gradeRepository == null)
            throw new IllegalArgumentException("GradeRepository não pode ser nulo");

        if (filmeRepository == null)
            throw new IllegalArgumentException("FilmeRepository não pode ser nulo");
        
        if (salaRepository == null)
            throw new IllegalArgumentException("SalaRepository não pode ser nulo");

        this.gradeRepository = gradeRepository;
        this.filmeRepository = filmeRepository;
        this.salaRepository = salaRepository;
    }

    public GradeDeExibicao criarGrade(LocalDate inicio, LocalDate fim) {
        GradeDeExibicao grade = new GradeDeExibicao(inicio, fim);
        gradeRepository.salvar(grade);
        return grade;
    }

    public void adicionarSessao(UUID gradeId, UUID filmeId, UUID salaId, LocalDateTime inicio) {
        GradeDeExibicao grade = gradeRepository.buscarPorId(gradeId)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + gradeId));

        Filme filme = filmeRepository.buscarPorId(filmeId)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        if (!filme.isAtivo())
            throw new IllegalStateException("Filme não está ativo e não pode ser adicionado à grade de exibição");

        Sala sala = salaRepository.buscarPorId(salaId)
            .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada: " + salaId));

        Sessao sessao = new Sessao(filme, sala, inicio);
        grade.adicionarSessao(sessao);
        gradeRepository.salvar(grade);
    }

    public GradeDeExibicao buscarPorData(LocalDate data) {
        return gradeRepository.buscarPorData(data)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada para a data: " + data));
    }

    public Sessao removerSessao(UUID gradeId, UUID sessaoId, LocalDateTime agora) {
        if (agora == null)
            throw new IllegalArgumentException("Horário atual não pode ser nulo");

        GradeDeExibicao grade = gradeRepository.buscarPorId(gradeId)
            .orElseThrow(() -> new IllegalArgumentException("Grade não encontrada: " + gradeId));

        Sessao sessao = grade.removerSessao(sessaoId, agora);
        gradeRepository.salvar(grade);
        return sessao;
    }

    public List<GradeDeExibicao> listarTodas() {
        return gradeRepository.listarTodas();
    }
}