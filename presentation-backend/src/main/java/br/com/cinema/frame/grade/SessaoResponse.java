package br.com.cinema.frame.grade;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessaoResponse(
    UUID id,
    UUID filmeId,
    String filmeTitulo,
    UUID salaId,
    int salaNumero,
    LocalDateTime inicio,
    LocalDateTime fim
) {
    public static SessaoResponse from(Sessao s) {
        return new SessaoResponse(
            s.getId(),
            s.getFilme().getId(),
            s.getFilme().getTitulo(),
            s.getSala().getId(),
            s.getSala().getNumero(),
            s.getInicio(),
            s.getFim()
        );
    }
}
