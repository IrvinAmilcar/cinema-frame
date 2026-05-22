package br.com.cinema.frame.grade;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GradeResponse(
    UUID id,
    LocalDate inicio,
    LocalDate fim,
    List<SessaoResponse> sessoes
) {
    public static GradeResponse from(GradeDeExibicao g) {
        return new GradeResponse(
            g.getId(),
            g.getInicio(),
            g.getFim(),
            g.getSessoes().stream().map(SessaoResponse::from).toList()
        );
    }
}
