package br.com.cinema.frame.grade;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record SessaoResponse(
    UUID id,
    UUID filmeId,
    String filmeTitulo,
    UUID salaId,
    int salaNumero,
    String inicio,
    String fim
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static SessaoResponse from(Sessao s) {
        return new SessaoResponse(
            s.getId(),
            s.getFilme().getId(),
            s.getFilme().getTitulo(),
            s.getSala().getId(),
            s.getSala().getNumero(),
            s.getInicio().format(FMT),
            s.getFimComIntervalo().format(FMT)
        );
    }
}
