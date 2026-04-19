package br.com.cinema.frame.domain.backoffice.grade;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeDeExibicaoRepository {

    void salvar(GradeDeExibicao grade);
    Optional<GradeDeExibicao> buscarPorId(UUID id);
    Optional<GradeDeExibicao> buscarPorData(LocalDate data);
    List<GradeDeExibicao> listarTodas();
    void remover(UUID id);
}