package br.com.cinema.frame.grade;

import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicao;

import java.util.List;
import java.util.UUID;

public record GradeResponse(
    UUID id,
    String inicio,
    String fim,
    List<SessaoResponse> sessoes
) {
    public static GradeResponse from(GradeDeExibicao g) {
        return new GradeResponse(
            g.getId(),
            g.getInicio().toString(),
            g.getFim().toString(),
            g.getSessoes().stream().map(SessaoResponse::from).toList()
        );
    }
}
