package br.com.cinema.frame.infrastructure.programacao;

import br.com.cinema.frame.domain.portal.notificacao.FilmeFavoritado;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "filmes_favoritados")
public class FavoritoJpa {

    @Id
    private UUID id;
    private UUID usuarioId;
    private UUID filmeId;
    private boolean notificado;

    protected FavoritoJpa() {}

    public static FavoritoJpa fromDomain(FilmeFavoritado f) {
        FavoritoJpa e = new FavoritoJpa();
        e.id = f.getId();
        e.usuarioId = f.getUsuarioId();
        e.filmeId = f.getFilmeId();
        e.notificado = f.isNotificado();
        return e;
    }

    public FilmeFavoritado toDomain() {
        return FilmeFavoritado.reconstituir(id, usuarioId, filmeId, notificado);
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public UUID getFilmeId() { return filmeId; }
    public boolean isNotificado() { return notificado; }
}
