package br.com.cinema.frame.domain.portal.notificacao;

import java.util.UUID;

public class FilmeFavoritado {

    private final UUID id;
    private final UUID usuarioId;
    private final UUID filmeId;
    private boolean notificado;

    public FilmeFavoritado(UUID usuarioId, UUID filmeId) {
        if (usuarioId == null)
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        if (filmeId == null)
            throw new IllegalArgumentException("ID do filme não pode ser nulo");

        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.filmeId = filmeId;
        this.notificado = false;
    }

    public void marcarComoNotificado() {
        this.notificado = true;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public UUID getFilmeId() { return filmeId; }
    public boolean isNotificado() { return notificado; }
}