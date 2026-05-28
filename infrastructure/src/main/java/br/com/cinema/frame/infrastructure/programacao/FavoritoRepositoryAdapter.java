package br.com.cinema.frame.infrastructure.programacao;

import br.com.cinema.frame.domain.portal.notificacao.FilmeFavoritado;
import br.com.cinema.frame.domain.portal.notificacao.FilmeFavoritadoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FavoritoRepositoryAdapter implements FilmeFavoritadoRepository {

    private final FavoritoJpaRepository jpa;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(FilmeFavoritado filmeFavoritado) {
        jpa.save(FavoritoJpa.fromDomain(filmeFavoritado));
    }

    @Override
    public Optional<FilmeFavoritado> buscarPorId(UUID id) {
        return jpa.findById(id).map(FavoritoJpa::toDomain);
    }

    @Override
    public List<FilmeFavoritado> buscarNaoNotificadosPorFilme(UUID filmeId) {
        return jpa.findByFilmeIdAndNotificadoFalse(filmeId).stream()
                .map(FavoritoJpa::toDomain)
                .toList();
    }

    @Override
    public List<FilmeFavoritado> buscarPorUsuarioId(UUID usuarioId) {
        return jpa.findByUsuarioId(usuarioId).stream()
                .map(FavoritoJpa::toDomain)
                .toList();
    }

    @Override
    public void removerPorUsuarioEFilme(UUID usuarioId, UUID filmeId) {
        jpa.deleteByUsuarioIdAndFilmeId(usuarioId, filmeId);
    }
}
