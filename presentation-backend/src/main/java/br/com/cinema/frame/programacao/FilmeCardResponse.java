package br.com.cinema.frame.programacao;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

import java.util.List;
import java.util.UUID;

public record FilmeCardResponse(
        UUID filmeId,
        String titulo,
        String genero,
        String classificacao,
        int duracaoMinutos,
        String trailerUrl,
        String sinopse,
        double nota,
        boolean recomendado,
        List<SessaoCardResponse> sessoes
) {
    public record SessaoCardResponse(UUID id, String inicio, int sala) {
        public static SessaoCardResponse from(Sessao s) {
            return new SessaoCardResponse(s.getId(), s.getInicio().toString(), s.getSala().getNumero());
        }
    }

    public static FilmeCardResponse from(UUID filmeId, String titulo, String genero,
                                          String classificacao, int duracaoMinutos,
                                          String trailerUrl, String sinopse, double nota,
                                          boolean recomendado, List<Sessao> sessoes) {
        return new FilmeCardResponse(
                filmeId, titulo, genero, classificacao, duracaoMinutos,
                trailerUrl, sinopse, nota, recomendado,
                sessoes.stream().map(SessaoCardResponse::from).toList()
        );
    }
}
