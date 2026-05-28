package br.com.cinema.frame.domain.portal.notificacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilmeFavoritadoRepository {

    void salvar(FilmeFavoritado filmeFavoritado);
    Optional<FilmeFavoritado> buscarPorId(UUID id);
    List<FilmeFavoritado> buscarNaoNotificadosPorFilme(UUID filmeId);
    List<FilmeFavoritado> buscarPorUsuarioId(UUID usuarioId);
    void removerPorUsuarioEFilme(UUID usuarioId, UUID filmeId);
}